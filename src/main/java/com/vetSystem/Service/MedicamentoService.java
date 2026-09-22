
package com.vetSystem.Service;

import com.vetSystem.DTO.MedicamentoRequestDTO;
import com.vetSystem.DTO.MedicamentoResponseDTO;
import com.vetSystem.Entity.Medicamento;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.MedicamentoMapper;
import com.vetSystem.Repository.MedicamentoRepository;
import com.vetSystem.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MedicamentoService {
    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;

    @Transactional(readOnly = true)
    public List<MedicamentoResponseDTO> listar() {
        List<MedicamentoResponseDTO> resultado = new ArrayList<>();
        for (Medicamento medicamento : medicamentoRepository.findAll()) {
            resultado.add(medicamentoMapper.toDTO(medicamento));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Optional<MedicamentoResponseDTO> buscarPorId(Long id) {
        Optional<Medicamento> medicamento = medicamentoRepository.findById(id);
        if (medicamento.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(medicamentoMapper.toDTO(medicamento.get()));
    }

    @Transactional
    public MedicamentoResponseDTO registrar(MedicamentoRequestDTO dto) {
        normalizar(dto);
        if (medicamentoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new DuplicateResourceException(
                    "Ya existe un medicamento con el nombre " + dto.getNombre());
        }
        Medicamento medicamento = medicamentoMapper.toEntity(dto);
        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamento));
    }

    @Transactional
    public MedicamentoResponseDTO modificar(Long id, MedicamentoRequestDTO dto) {
        normalizar(dto);
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
        medicamento.setNombre(dto.getNombre());
        medicamento.setPrincipioActivo(dto.getPrincipioActivo());
        medicamento.setStock(dto.getStock());
        medicamento.setPrecioUnitario(dto.getPrecioUnitario());
        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamento));
    }

    @Transactional
    public void eliminar(Long id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
        medicamentoRepository.delete(medicamento);
    }

    // Un solo lugar decide como se guarda el texto de esta entidad. Se llama desde el alta
    // y desde la edicion, asi no hay forma de que una de las dos se olvide.
    private void normalizar(MedicamentoRequestDTO dto) {
        dto.setNombre(TextoUtil.aTitulo(dto.getNombre()));
        dto.setPrincipioActivo(TextoUtil.aTitulo(dto.getPrincipioActivo()));
    }
}
