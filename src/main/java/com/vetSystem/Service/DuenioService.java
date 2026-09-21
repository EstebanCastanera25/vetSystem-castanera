package com.vetSystem.Service;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.DuenioMapper;
import com.vetSystem.Repository.DuenioRepository;
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
@RequiredArgsConstructor  // Lombok: genera constructor con los campos final (constructor injection)
public class DuenioService implements InterfaceService<DuenioDTO> {

    private final DuenioRepository duenioRepository;
    private final DuenioMapper duenioMapper;

    // Listar todos los dueños como DTO (la entidad JPA no sale del service)
    @Override
    public List<DuenioDTO> listarEntidades() {
        List<Duenio> duenios = duenioRepository.findAll();
        List<DuenioDTO> resultado = new ArrayList<>();
        for (Duenio duenio : duenios) {
            resultado.add(duenioMapper.toDTO(duenio));
        }
        return resultado;
    }

    // Buscar por ID — Optional vacío si no existe (el controller decide el 404)
    @Override
    public Optional<DuenioDTO> buscarPorId(Long id) {
        Optional<Duenio> duenio = duenioRepository.findById(id);
        if (duenio.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(duenioMapper.toDTO(duenio.get()));
    }

    // Registrar un nuevo dueño — valida cédula duplicada
    @Override
    public DuenioDTO registrarEntidad(DuenioDTO dto) {
        // Se normaliza ANTES de chequear duplicados, y antes de que el mapper copie los
        // campos a la entidad: así el valor ya sale normalizado también en la respuesta.
        normalizar(dto);
        if (duenioRepository.existsByCedula(dto.getCedula())) {
            throw new DuplicateResourceException("Ya existe un dueño con cédula: " + dto.getCedula());
        }
        Duenio duenio = duenioMapper.toEntity(dto);
        return duenioMapper.toDTO(duenioRepository.save(duenio));
    }

    // Modificar un dueño existente (el id viaja dentro del DTO)
    @Override
    public DuenioDTO modificarEntidad(DuenioDTO dto) {
        normalizar(dto);
        Duenio duenio = duenioRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", dto.getId()));
        duenio.setNombre(dto.getNombre());
        duenio.setApellido(dto.getApellido());
        duenio.setTelefono(dto.getTelefono());
        duenio.setEmail(dto.getEmail());
        // La cédula no se actualiza — es el identificador de negocio
        return duenioMapper.toDTO(duenioRepository.save(duenio));
    }

    // Eliminar un dueño — valida que exista antes de borrar
    @Override
    public void eliminarEntidad(Long id) {
        Duenio duenio = duenioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", id));
        duenioRepository.delete(duenio);
    }

    // Buscar por nombre EXACTO (contrato InterfaceService): devuelve 0 ó 1 resultado.
    // Es otra operación distinta a buscarPorTexto, que es parcial y devuelve N. Por eso conviven.
    @Override
    public Optional<DuenioDTO> buscarPorString(String nombre) {
        Optional<Duenio> duenio = duenioRepository.findByNombre(nombre);
        if (duenio.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(duenioMapper.toDTO(duenio.get()));
    }

    // El orden con el que se listan los dueños si el cliente no pide ninguno.
    // Termina en el id a propósito: dos dueños con el mismo apellido y nombre tienen que
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
            "cedula", "cedula",
            // El teléfono es Integer: ordena NUMÉRICAMENTE, no como texto
            "telefono", "telefono",
            "email", "email");

    // Buscador paginado del frontend: texto parcial contra nombre, apellido, cédula y email.
    // Es lo que pide el RF-02 del SRS ("GET /api/duenios retorna una lista paginada").
    //
    // Queda FUERA del contrato InterfaceService (no lleva @Override) porque su semántica
    // es distinta a la de buscarPorString: aquél es match exacto y devuelve 0 ó 1, éste es
    // parcial, ignora mayúsculas y devuelve una página de N.
    //
    // La regla del texto vacío vive ACÁ, no en el controller: un solo lugar decide qué
    // significa "sin filtro" y vale igual para el frontend, para Swagger y para Postman.
    // isBlank() (no isEmpty()) para que "   " también cuente como vacío, y trim() para que
    // "  ana  " no se convierta en un LIKE '%  ana  %' que no encuentra nada.
    public PaginaDTO<DuenioDTO> buscarPorTexto(String texto, Integer pagina, Integer tamanio,
                                               String orden, String direccion) {
        Sort ordenPedido = PaginaUtil.armarOrden(orden, direccion, CAMPOS_ORDENABLES, ORDEN_POR_DEFECTO);
        Pageable pageable = PaginaUtil.armarPageable(pagina, tamanio, ordenPedido);

        // Se pagina en la BASE (LIMIT/OFFSET), no en memoria: traer todo para después
        // quedarse con diez sería paginar la pantalla, no la consulta.
        Page<Duenio> paginaDeDuenios;
        if (texto == null || texto.isBlank()) {
            paginaDeDuenios = duenioRepository.findAll(pageable);
        } else {
            paginaDeDuenios = duenioRepository.buscarPorTexto(texto.trim(), pageable);
        }

        List<DuenioDTO> contenido = new ArrayList<>();
        for (Duenio duenio : paginaDeDuenios.getContent()) {
            contenido.add(duenioMapper.toDTO(duenio));
        }
        return PaginaUtil.armar(contenido, paginaDeDuenios);
    }

    // Un solo lugar decide cómo se guarda el texto de esta entidad. Lo llaman el alta y la
    // edición, así no hay forma de que una de las dos se olvide.
    //
    // Se normaliza el DTO y no la entidad a propósito: el alta copia los campos con el
    // mapper y la edición los copia con setters, o sea que el texto entra por dos puertas
    // distintas. Tocando el DTO se cubren las dos de una sola vez.
    //
    // La cédula queda afuera: son dígitos, no hay mayúsculas que arreglar.
    private void normalizar(DuenioDTO dto) {
        dto.setNombre(TextoUtil.aTitulo(dto.getNombre()));
        dto.setApellido(TextoUtil.aTitulo(dto.getApellido()));
        dto.setEmail(TextoUtil.aMinusculas(dto.getEmail()));
    }
}
