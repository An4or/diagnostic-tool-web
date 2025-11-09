package com.intervale.diagnostictool.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intervale.diagnostictool.dto.ProfileFaultDto;
import com.intervale.diagnostictool.dto.request.ProfileFaultRequest;
import com.intervale.diagnostictool.exception.ResourceNotFoundException;
import com.intervale.diagnostictool.mapper.FaultMapper;
import com.intervale.diagnostictool.model.*;
import com.intervale.diagnostictool.model.DiagnosticMethod.CoverageLevel;
import com.intervale.diagnostictool.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileFaultService {

    private final ProfileFaultRepository profileFaultRepository;
    private final ProfileRepository profileRepository;
    private final FaultTypeRepository faultTypeRepository;
    private final DiagnosticMethodRepository diagnosticMethodRepository;
    private final FaultMapper faultMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void updateFaultStatus(Long profileId, Long deviceId, Long faultId, boolean covered) {
        // Create the composite key
        ProfileFaultId id = new ProfileFaultId(profileId, faultId);

        // Try to find the profile fault by composite key
        Optional<ProfileFault> optionalProfileFault = profileFaultRepository.findById(id);

        ProfileFault profileFault;

        if (optionalProfileFault.isPresent()) {
            // Update existing record
            profileFault = optionalProfileFault.get();
        } else {
            // Create new record if it doesn't exist
            Profile profile = profileRepository.findById(profileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileId));

            FaultType faultType = faultTypeRepository.findById(faultId)
                    .orElseThrow(() -> new ResourceNotFoundException("FaultType", "id", faultId));

            profileFault = new ProfileFault();
            profileFault.setId(id);
            profileFault.setProfile(profile);
            profileFault.setFaultType(faultType);
        }

        // Update the covered status
        profileFault.setCovered(covered);
        profileFaultRepository.save(profileFault);
    }

    @Transactional(readOnly = true)
    public List<ProfileFaultDto> findByProfileId(Long profileId) {
        return faultMapper.toProfileFaultDtoList(profileFaultRepository.findByProfileId(profileId));
    }

    @Transactional(readOnly = true)
    public ProfileFaultDto findById(ProfileFaultId id) {
        return faultMapper.toDto(profileFaultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfileFault", "id", id.toString())));
    }

    @Transactional
    public ProfileFaultDto create(ProfileFaultRequest request) {
        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", request.getProfileId()));

        FaultType faultType = faultTypeRepository.findById(request.getFaultTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("FaultType", "id", request.getFaultTypeId()));

        // Create the composite key
        ProfileFaultId id = new ProfileFaultId(profile.getId(), faultType.getId());

        // Check if the relationship already exists
        Optional<ProfileFault> existing = profileFaultRepository.findById(id);

        if (existing.isPresent()) {
            return update(id, request);
        }

        ProfileFault profileFault = faultMapper.toEntity(request);
        profileFault.setProfile(profile);
        profileFault.setFaultType(faultType);

        // Set covered methods if provided
        if (request.getCoveredMethodIds() != null && !request.getCoveredMethodIds().isEmpty()) {
            setCoveredMethods(profileFault, request.getCoveredMethodIds());
        }

        return faultMapper.toDto(profileFaultRepository.save(profileFault));
    }

    @Transactional
    public ProfileFaultDto update(ProfileFaultId id, ProfileFaultRequest request) {
        ProfileFault profileFault = profileFaultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfileFault", "id", id.toString()));

        // Update basic fields
        profileFault.setCovered(request.isCovered());
        profileFault.setNotes(request.getNotes());

        // Update covered methods if provided
        if (request.getCoveredMethodIds() != null) {
            setCoveredMethods(profileFault, request.getCoveredMethodIds());
        }

        // Recalculate coverage based on methods
        updateCoverageStatus(profileFault);

        return faultMapper.toDto(profileFaultRepository.save(profileFault));
    }

    @Transactional
    public void delete(ProfileFaultId id) {
        if (!profileFaultRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProfileFault", "id", id.toString());
        }
        profileFaultRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProfileFaultDto> findByProfileIdAndDeviceCategoryId(Long profileId, Long categoryId) {
        return faultMapper.toProfileFaultDtoList(
                profileFaultRepository.findByProfileIdAndDeviceCategoryId(profileId, categoryId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCoverageStats(Long profileId) {
        long totalFaults = profileFaultRepository.countTotalFaultsByProfileId(profileId);
        long coveredFaults = profileFaultRepository.countCoveredFaultsByProfileId(profileId);

        double coveragePercentage = totalFaults > 0 ? (double) coveredFaults / totalFaults * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFaults", totalFaults);
        stats.put("coveredFaults", coveredFaults);
        stats.put("coveragePercentage", Math.round(coveragePercentage * 100.0) / 100.0);

        return stats;
    }

    /**
     * Получает все сохраненные проценты покрытия для профиля
     * Возвращает Map с ключом "deviceId_faultId_methodId" и значением coveragePercent
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getAllCoveragePercentages(Long profileId) {
        Map<String, Integer> result = new HashMap<>();

        List<ProfileFault> profileFaults = profileFaultRepository.findByProfileId(profileId);

        for (ProfileFault profileFault : profileFaults) {
            if (profileFault.getCoveredMethodsIds() == null || profileFault.getCoveredMethodsIds().isEmpty()) {
                continue;
            }

            Map<String, Map<String, Integer>> coverageData = getCoverageData(profileFault);
            Long faultId = profileFault.getFaultType().getId();

            for (Map.Entry<String, Map<String, Integer>> deviceEntry : coverageData.entrySet()) {
                Long deviceId = Long.parseLong(deviceEntry.getKey());
                Map<String, Integer> methodCoverage = deviceEntry.getValue();

                for (Map.Entry<String, Integer> methodEntry : methodCoverage.entrySet()) {
                    Long methodId = Long.parseLong(methodEntry.getKey());
                    Integer coveragePercent = methodEntry.getValue();

                    String key = getCoverageCacheKey(profileId, deviceId, faultId, methodId);
                    result.put(key, coveragePercent);
                }
            }
        }

        log.info("Loaded {} coverage percentages for profileId={}", result.size(), profileId);
        return result;
    }

    /**
     * Генерирует ключ кеша для комбинации профиль + устройство + неисправность + метод
     */
    private String getCoverageCacheKey(Long profileId, Long deviceId, Long faultId, Long methodId) {
        return String.format("%d_%d_%d_%d", profileId, deviceId, faultId, methodId);
    }

    private void setCoveredMethods(ProfileFault profileFault, List<Long> methodIds) {
        try {
            profileFault.setCoveredMethodsIds(objectMapper.writeValueAsString(methodIds));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing covered method IDs", e);
        }
    }

    private void updateCoverageStatus(ProfileFault profileFault) {
        if (profileFault.getCoveredMethodsIds() == null || profileFault.getCoveredMethodsIds().isEmpty()) {
            profileFault.setCovered(false);
            return;
        }

        try {
            List<Long> methodIds = objectMapper.readValue(
                    profileFault.getCoveredMethodsIds(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});

            if (methodIds.isEmpty()) {
                profileFault.setCovered(false);
                return;
            }

            // Get all methods for the fault type
            List<DiagnosticMethod> allMethods = diagnosticMethodRepository
                    .findByFaultTypeId(profileFault.getFaultType().getId());

            // Check if all required methods are covered
            Set<Long> requiredMethodIds = allMethods.stream()
                    .filter(method -> method.getCoverageLevel() == CoverageLevel.HIGH)
                    .map(DiagnosticMethod::getId)
                    .collect(Collectors.toSet());

            Set<Long> coveredMethodIds = new HashSet<>(methodIds);
            boolean allRequiredCovered = coveredMethodIds.containsAll(requiredMethodIds);

            profileFault.setCovered(allRequiredCovered || !requiredMethodIds.isEmpty());

        } catch (Exception e) {
            throw new RuntimeException("Error updating coverage status", e);
        }
    }

    /**
     * Сохраняет процент покрытия для комбинации: профиль + устройство + неисправность + метод диагностики
     */
    @Transactional
    public void updateFaultMethodCoverage(Long profileId, Long deviceId, Long faultId, Long methodId, Integer coveragePercent) {
        ProfileFaultId profileFaultId = new ProfileFaultId(profileId, faultId);

        // Найти или создать ProfileFault
        ProfileFault profileFault = profileFaultRepository.findById(profileFaultId)
                .orElseGet(() -> {
                    Profile profile = profileRepository.findById(profileId)
                            .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileId));
                    FaultType faultType = faultTypeRepository.findById(faultId)
                            .orElseThrow(() -> new ResourceNotFoundException("FaultType", "id", faultId));

                    ProfileFault newProfileFault = new ProfileFault();
                    newProfileFault.setId(profileFaultId);
                    newProfileFault.setProfile(profile);
                    newProfileFault.setFaultType(faultType);
                    return newProfileFault;
                });

        // Получить текущие данные о методах и процентах
        Map<String, Map<String, Integer>> coverageData = getCoverageData(profileFault);
        log.debug("Current coverage data before update: {}", coverageData);

        // Обновить процент для конкретного устройства и метода
        Map<String, Integer> deviceData = coverageData.computeIfAbsent(deviceId.toString(), k -> {
            log.debug("Creating new device entry for deviceId: {}", deviceId);
            return new HashMap<>();
        });
        deviceData.put(methodId.toString(), coveragePercent);
        log.debug("Updated device data: {}", deviceData);
        log.debug("Updated coverage data: {}", coverageData);

        // Сохранить обратно в JSON
        try {
            String jsonData = objectMapper.writeValueAsString(coverageData);
            log.debug("Serialized JSON data: {}", jsonData);
            profileFault.setCoveredMethodsIds(jsonData);
            ProfileFault saved = profileFaultRepository.save(profileFault);
            log.debug("Saved ProfileFault with ID: {}", saved.getId());
            log.debug("Saved coverage data: {}", saved.getCoveredMethodsIds());

            // Flush to ensure data is written to DB immediately
            profileFaultRepository.flush();

            // Verify the data was saved correctly
            Optional<ProfileFault> verify = profileFaultRepository.findById(profileFaultId);
            if (verify.isPresent()) {
                log.debug("Verified saved data: {}", verify.get().getCoveredMethodsIds());
            } else {
                log.error("ERROR: Data was not found immediately after save! profileId={}, faultId={}", profileId, faultId);
            }

            log.info("Successfully saved coverage data: {} for profileId={}, deviceId={}, faultId={}, methodId={}, coveragePercent={}",
                    jsonData, profileId, deviceId, faultId, methodId, coveragePercent);
        } catch (JsonProcessingException e) {
            log.error("Error serializing coverage data", e);
            throw new RuntimeException("Error serializing coverage data", e);
        }
    }

    /**
     * Получает процент покрытия для комбинации: профиль + устройство + неисправность + метод диагностики
     */
    @Transactional(readOnly = true)
    public Integer getFaultMethodCoverage(Long profileId, Long deviceId, Long faultId, Long methodId) {
        ProfileFaultId profileFaultId = new ProfileFaultId(profileId, faultId);

        Optional<ProfileFault> profileFaultOpt = profileFaultRepository.findById(profileFaultId);
        if (profileFaultOpt.isEmpty()) {
            log.debug("ProfileFault not found for profileId={}, faultId={}", profileId, faultId);
            return null;
        }

        ProfileFault profileFault = profileFaultOpt.get();
        log.debug("Found ProfileFault with coveredMethodsIds: {}", profileFault.getCoveredMethodsIds());

        Map<String, Map<String, Integer>> coverageData = getCoverageData(profileFault);

        log.debug("Coverage data for profileId={}, deviceId={}, faultId={}, methodId={}: {}",
                profileId, deviceId, faultId, methodId, coverageData);
        log.debug("Looking for deviceId={} (as string: {})", deviceId, deviceId.toString());

        Map<String, Integer> deviceCoverage = coverageData.get(deviceId.toString());
        if (deviceCoverage == null) {
            log.warn("Device coverage not found for deviceId={}. Available keys: {}", deviceId, coverageData.keySet());
            return null;
        }

        log.debug("Found device coverage: {}", deviceCoverage);
        log.debug("Looking for methodId={} (as string: {})", methodId, methodId.toString());

        Integer coverage = deviceCoverage.get(methodId.toString());
        log.info("Found coverage: {} for methodId={}", coverage, methodId);
        return coverage;
    }

    /**
     * Парсит JSON с данными о покрытии
     */
    private Map<String, Map<String, Integer>> getCoverageData(ProfileFault profileFault) {
        if (profileFault.getCoveredMethodsIds() == null || profileFault.getCoveredMethodsIds().isEmpty()) {
            log.debug("ProfileFault.coveredMethodsIds is null or empty");
            return new HashMap<>();
        }

        log.debug("Parsing coverage data from JSON: {}", profileFault.getCoveredMethodsIds());

        try {
            // Пытаемся распарсить как Map<String, Map<String, Integer>>
            Map<String, Map<String, Integer>> result = objectMapper.readValue(
                    profileFault.getCoveredMethodsIds(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Map<String, Integer>>>() {}
            );
            log.debug("Successfully parsed coverage data: {}", result);
            return result;
        } catch (Exception e) {
            log.warn("Error parsing coverage data as Map<String, Map<String, Integer>>: {}", e.getMessage());
            // Если не получается распарсить как Map, возможно это старый формат (List<Long>)
            try {
                List<Long> methodIds = objectMapper.readValue(
                        profileFault.getCoveredMethodsIds(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {}
                );
                log.debug("Found old format (List<Long>): {}", methodIds);
                // Конвертируем старый формат в новый (без процентов)
                return new HashMap<>();
            } catch (Exception e2) {
                log.warn("Error parsing coverage data as List<Long>: {}", e2.getMessage());
                return new HashMap<>();
            }
        }
    }
}
