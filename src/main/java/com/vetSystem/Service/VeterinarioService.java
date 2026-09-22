package com.vetSystem.Service;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.VeterinarioMapper;
import com.vetSystem.Repository.VeterinarioRepository;
import com.vetSystem.util.PaginaUtil;
import com.vetSystem.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor  // Lombok: genera constructor con los campos final
public class VeterinarioService implements InterfaceService<VeterinarioDTO> {

    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioMapper veterinarioMapper;

    // Registra un veterinario y valida que la matrícula no esté repetida
    @Override
    public VeterinarioDTO registrarEntidad(VeterinarioDTO dto) {
        // Se normaliza ANTES de chequear duplicados: si ya existe "MV-5501", la entrada
        // "mv-5501" debe detectarse aca y devolver el 409, no fallar despues en la base.
        normalizar(dto);
        if (veterinarioRepository.existsByMatricula(dto.getMatricula())) {
            throw new DuplicateResourceException(
                    "Ya existe un veterinario con matricula: " + dto.getMatricula());
        }

        Veterinario veterinario = veterinarioMapper.toEntity(dto);
        return veterinarioMapper.toDTO(veterinarioRepository.save(veterinario));
    }

    // Busca por ID y devuelve un Optional vacío cuando no existe
    @Override
    public Optional<VeterinarioDTO> buscarPorId(Long id) {
        Optional<Veterinario> veterinario = veterinarioRepository.findById(id);
        if (veterinario.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(veterinarioMapper.toDTO(veterinario.get()));
    }

    // Lista todos los veterinarios como DTO
    @Override
    public List<VeterinarioDTO> listarEntidades() {
        List<Veterinario> veterinarios = veterinarioRepository.findAll();
        List<VeterinarioDTO> resultado = new ArrayList<>();
        for (Veterinario veterinario : veterinarios) {
            resultado.add(veterinarioMapper.toDTO(veterinario));
        }
        return resultado;
    }

    // Modifica los datos editables de un veterinario existente
    @Override
    public VeterinarioDTO modificarEntidad(VeterinarioDTO dto) {
        normalizar(dto);
        Veterinario veterinario = veterinarioRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", dto.getId()));

        veterinario.setNombre(dto.getNombre());
        veterinario.setApellido(dto.getApellido());
        veterinario.setEspecialidad(dto.getEspecialidad());
        veterinario.setEmail(dto.getEmail());
        // La matrícula no se actualiza: es el identificador de negocio

        return veterinarioMapper.toDTO(veterinarioRepository.save(veterinario));
    }

    // Elimina un veterinario después de validar que exista
    @Override
    public void eliminarEntidad(Long id) {
        Veterinario veterinario = veterinarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", id));
        veterinarioRepository.delete(veterinario);
    }

    // Busca un veterinario por su matrícula
    @Override
    public Optional<VeterinarioDTO> buscarPorString(String valor) {
        Optional<Veterinario> veterinario = veterinarioRepository.findByMatricula(valor);
        if (veterinario.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(veterinarioMapper.toDTO(veterinario.get()));
    }

    // El orden con el que se listan los veterinarios si el cliente no pide ninguno.
    // Termina en el id a propósito: dos veterinarios con el mismo apellido y nombre tienen que
    // quedar siempre en el mismo orden relativo, o al paginar uno podría aparecer en dos
    // páginas distintas y otro en ninguna.
    private static final Sort ORDEN_POR_DEFECTO =
            Sort.by(Sort.Order.asc("apellido"), Sort.Order.asc("nombre"), Sort.Order.asc("id"));

    // Por qué campos se puede ordenar. La clave es el nombre que manda el frontend y el
    // valor es el campo real de la entidad (acá coinciden; en mascotas y turnos no).
    // Lo que no esté en esta lista cae al orden por defecto: un campo inventado haría que
    // Hibernate falle al armar la consulta y la API devolvería un 500 desde la URL.
    private static final Map<String, String> CAMPOS_ORDENABLES = Map.of(
            "id", "id",
            "nombre", "nombre",
            "apellido", "apellido",
            "matricula", "matricula",
            "especialidad", "especialidad",
            "email", "email");

    // Buscador paginado del frontend: texto parcial contra nombre, apellido, matrícula y especialidad.
    // Queda FUERA del contrato InterfaceService (no lleva @Override) porque su semántica
    // es distinta a la de buscarPorString: aquél es match exacto y devuelve 0 ó 1, éste es
    // parcial, ignora mayúsculas y devuelve una página de N.
    //
    // La regla del texto vacío vive ACÁ, no en el controller: un solo lugar decide qué
    // significa "sin filtro" y vale igual para el frontend, para Swagger y para Postman.
    // isBlank() (no isEmpty()) para que "   " también cuente como vacío, y trim() para que
    // "  ana  " no se convierta en un LIKE '%  ana  %' que no encuentra nada.
    public PaginaDTO<VeterinarioDTO> buscarPorTexto(String texto, Integer pagina, Integer tamanio,
                                                    String orden, String direccion) {
        Sort ordenPedido = PaginaUtil.armarOrden(orden, direccion, CAMPOS_ORDENABLES, ORDEN_POR_DEFECTO);
        Pageable pageable = PaginaUtil.armarPageable(pagina, tamanio, ordenPedido);

        // Se pagina en la BASE (LIMIT/OFFSET), no en memoria: traer todo para después
        // quedarse con diez sería paginar la pantalla, no la consulta.
        Page<Veterinario> paginaDeVeterinarios;
        if (texto == null || texto.isBlank()) {
            paginaDeVeterinarios = veterinarioRepository.findAll(pageable);
        } else {
            paginaDeVeterinarios = veterinarioRepository.buscarPorTexto(texto.trim(), pageable);
        }

        List<VeterinarioDTO> contenido = new ArrayList<>();
        for (Veterinario veterinario : paginaDeVeterinarios.getContent()) {
            contenido.add(veterinarioMapper.toDTO(veterinario));
        }
        return PaginaUtil.armar(contenido, paginaDeVeterinarios);
    }

    // Un solo lugar decide como se guarda el texto de esta entidad. Se llama desde el alta
    // y desde la edicion, asi no hay forma de que una de las dos se olvide.
    private void normalizar(VeterinarioDTO dto) {
        dto.setNombre(TextoUtil.aTitulo(dto.getNombre()));
        dto.setApellido(TextoUtil.aTitulo(dto.getApellido()));
        dto.setEspecialidad(TextoUtil.aTitulo(dto.getEspecialidad()));
        dto.setEmail(TextoUtil.aMinusculas(dto.getEmail()));
        dto.setMatricula(TextoUtil.aMayusculas(dto.getMatricula()));
    }
}
