package Samba.service.machine.implement;

import Samba.commons.constans.response.machine.IMachineResponse;
import Samba.commons.converter.machine.MachineConverter;
import Samba.commons.domains.entity.typeMachinery.TypeMachineryEntity;
import Samba.commons.domains.responseDTO.GenericResponseDTO;
import Samba.commons.domains.DTO.machine.MachineDTO;
import Samba.commons.domains.entity.machine.IAdapterMachine;
import Samba.commons.domains.entity.machine.MachineEntity;
import Samba.components.MaintenanceLogic;
import Samba.repository.machine.IMachineRepository;
import Samba.repository.typeMachinery.ITypeMachineryRepository;
import Samba.service.machine.IMachineService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class MachineService implements IMachineService
{
    private final IMachineRepository vehicleRepository;
    private final MachineConverter machineConverter;
    private final ITypeMachineryRepository typeMachineryRepository;

    private final MaintenanceLogic maintenanceLogic;

    public MachineService(IMachineRepository vehicleRepository, MachineConverter machineConverter, ITypeMachineryRepository typeMachineryRepository, MaintenanceLogic maintenanceLogic){
        this.machineConverter = machineConverter;
        this.typeMachineryRepository = typeMachineryRepository;
        this.vehicleRepository = vehicleRepository;
        this.maintenanceLogic = maintenanceLogic;
    }

    @Override
    public ResponseEntity<GenericResponseDTO> createVehicle(MachineDTO machineDTO) {
        try {
            Optional<MachineEntity> vehicleExist = this.vehicleRepository.findById(machineDTO.getMachineSambaId());
            Optional<TypeMachineryEntity> typeMachineExist = this.typeMachineryRepository.findById(machineDTO.getTypeMachineId());
            if (vehicleExist.isEmpty()) {
                if (typeMachineExist.isPresent()) {
                    MachineEntity machineEntity = machineConverter.convertMachineDTOToMachineEntity(machineDTO);
                    Integer accumulatedHours = machineEntity.getMachineAccumulatedHours();
                    machineEntity.setMachineType(typeMachineExist.get().getTypeMachinaryName());
                    machineEntity.setMachineEngineOilChange(this.maintenanceLogic.hoursMaintenanceState(accumulatedHours, "00", 250));
                    machineEntity.setMachineOilFilterChange(this.maintenanceLogic.hoursMaintenanceState(accumulatedHours, "00", 500));
                    machineEntity.setMachineFuelFilterChange(this.maintenanceLogic.hoursMaintenanceState(accumulatedHours, "00", 250));
                    machineEntity.setMachineHydraulicOilChange(this.maintenanceLogic.hoursMaintenanceState(accumulatedHours, "00", 1000));
                    machineEntity.setMachineDifferentialOilChange(this.maintenanceLogic.hoursMaintenanceState(accumulatedHours, "00", 1000));
                    machineEntity.setMachineFrontAxleLubrication(this.maintenanceLogic.hoursMaintenanceState(accumulatedHours, "00", 250));
                    machineEntity.setMachinePlanetaryGearOilChange(this.maintenanceLogic.hoursMaintenanceState(accumulatedHours, "00", 500));
                    machineEntity.setMachineRockerLubrication(this.maintenanceLogic.hoursMaintenance50State(accumulatedHours, "00"));
                    machineEntity.setMachineFlannelLubrication(this.maintenanceLogic.hoursMaintenance50State(accumulatedHours, "00"));
                    machineEntity.setMachineCrossheadLubrication(this.maintenanceLogic.hoursMaintenance50State(accumulatedHours, "00"));
                    this.vehicleRepository.save(machineEntity);
                    return ResponseEntity.ok(GenericResponseDTO.builder()
                            .message(IMachineResponse.OPERATION_SUCCESS)
                            .objectResponse(IMachineResponse.CREATE_SUCCESS)
                            .statusCode(HttpStatus.OK.value())
                            .build());
                } else {
                    return ResponseEntity.badRequest().body(GenericResponseDTO.builder()
                            .message(IMachineResponse.OPERATION_FAIL+ " El tipo de maquina pasado como id no existe")
                            .objectResponse(IMachineResponse.OPERATION_FAIL)
                            .statusCode(HttpStatus.OK.value())
                            .build());
                }
            } else {
                return ResponseEntity.badRequest().body(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_FAIL + " El id de la maquina ya existe")
                        .objectResponse(IMachineResponse.OPERATION_FAIL)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }
        } catch (Exception e) {
            log.error("Ha ocurrido un error interno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponseDTO.builder()
                            .message(IMachineResponse.INTERNAL_SERVER)
                            .objectResponse(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
    @Override
    public ResponseEntity<GenericResponseDTO> readVehicle(Integer machineId) {
        try {
            List<IAdapterMachine> vehicleExist = this.vehicleRepository.listMachinery(machineId);
            if (!vehicleExist.isEmpty()) {
                return ResponseEntity.ok(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_SUCCESS)
                        .objectResponse(vehicleExist)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            } else {
                return ResponseEntity.badRequest().body(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_FAIL)
                        .objectResponse(IMachineResponse.OPERATION_FAIL)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
            }
        } catch (Exception e) {
            log.error("Ha ocurrido un error interno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponseDTO.builder()
                            .message(IMachineResponse.INTERNAL_SERVER)
                            .objectResponse(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
    @Override
    public ResponseEntity<GenericResponseDTO> readAllVehicle() {
        try {
            List<MachineDTO> listDTO = new ArrayList<>();
            List<MachineEntity> vehicleExist = this.vehicleRepository.findAll();
            if (!vehicleExist.isEmpty()) {
                System.out.println("Entramos al Metodo leer");
                vehicleExist.forEach(item -> listDTO.add(machineConverter.convertMachineEntityToMachineDTO(item)));
                return ResponseEntity.badRequest().body(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_FAIL)
                        .objectResponse(IMachineResponse.OPERATION_FAIL)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
            } else {
                return ResponseEntity.ok(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_FAIL)
                        .objectResponse(IMachineResponse.OPERATION_FAIL)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }
        }
        catch (Exception e) {
            log.error("Ha ocurrido un error interno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponseDTO.builder()
                            .message(IMachineResponse.INTERNAL_SERVER)
                            .objectResponse(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
    @Override
    public ResponseEntity<GenericResponseDTO> updateVehicle(MachineDTO machineDTO) {
        try {
            Optional<MachineEntity> vehicleExist = this.vehicleRepository.findById(machineDTO.getMachineSambaId());
            Optional<TypeMachineryEntity> typeMachineExist = this.typeMachineryRepository.findById(machineDTO.getTypeMachineId());
            if (vehicleExist.isPresent()) {
                if(typeMachineExist.isPresent()) {
                    MachineEntity machineEntity = machineConverter.convertMachineDTOToMachineEntity(machineDTO);
                    TypeMachineryEntity typeMachine = typeMachineExist.get();
                    machineEntity.setMachineType(typeMachine.getTypeMachinaryName());
                    this.vehicleRepository.save(machineEntity);
                    return ResponseEntity.ok(GenericResponseDTO.builder()
                            .message(IMachineResponse.OPERATION_SUCCESS)
                            .objectResponse(IMachineResponse.UPDATE_SUCCESS)
                            .statusCode(HttpStatus.OK.value())
                            .build());
                } else {
                    return ResponseEntity.badRequest().body(GenericResponseDTO.builder()
                            .message(IMachineResponse.OPERATION_FAIL + ", el tipo de maquina ingresado no existe")
                            .objectResponse(IMachineResponse.UPDATE_FAIL)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .build());
                }
            } else {
                return ResponseEntity.badRequest().body(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_FAIL)
                        .objectResponse(IMachineResponse.UPDATE_FAIL)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
            }
        } catch (Exception e) {
            log.error("Ha ocurrido un error interno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponseDTO.builder()
                            .message(IMachineResponse.INTERNAL_SERVER)
                            .objectResponse(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponseDTO> deleteVehicle(MachineDTO machineDTO) {
        try {
            Optional<MachineEntity> vehicleExist = this.vehicleRepository.findById(machineDTO.getMachineSambaId());
            if (vehicleExist.isPresent()) {
                MachineEntity machineEntity = machineConverter.convertMachineDTOToMachineEntity(machineDTO);
                this.vehicleRepository.delete(machineEntity);
                return ResponseEntity.ok(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_SUCCESS)
                        .objectResponse(IMachineResponse.DELETE_SUCCESS)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            } else {
                return ResponseEntity.badRequest().body(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_FAIL)
                        .objectResponse(IMachineResponse.DELETE_FAIL)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
            }
        } catch (Exception e) {
            log.error("Ha ocurrido un error interno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponseDTO.builder()
                            .message(IMachineResponse.INTERNAL_SERVER)
                            .objectResponse(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponseDTO> readVehicleId(Integer machineId) {
        try {
            Optional<MachineEntity> vehicleExist = this.vehicleRepository.findById(machineId);
            if (vehicleExist.isPresent()) {
                System.out.println("Entramos al Metodo");
                return ResponseEntity.ok(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_SUCCESS)
                        .objectResponse(vehicleExist)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            } else {
                return ResponseEntity.ok(GenericResponseDTO.builder()
                        .message(IMachineResponse.OPERATION_FAIL)
                        .objectResponse(IMachineResponse.OPERATION_FAIL)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }
        } catch (Exception e) {
            log.error("Ha ocurrido un error interno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponseDTO.builder()
                            .message(IMachineResponse.INTERNAL_SERVER)
                            .objectResponse(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
