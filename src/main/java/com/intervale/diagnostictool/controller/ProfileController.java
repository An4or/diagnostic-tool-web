package com.intervale.diagnostictool.controller;

import com.intervale.diagnostictool.dto.DeviceWithFaultsDto;
import com.intervale.diagnostictool.dto.FaultTypeDto;
import com.intervale.diagnostictool.dto.FaultWithStatusDto;
import com.intervale.diagnostictool.dto.ProfileFaultDto;
import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.Profile;
import com.intervale.diagnostictool.model.DiagnosticMethod;
import com.intervale.diagnostictool.service.DeviceService;
import com.intervale.diagnostictool.service.DiagnosticMethodService;
import com.intervale.diagnostictool.service.FaultTypeService;
import com.intervale.diagnostictool.service.ProfileFaultService;
import com.intervale.diagnostictool.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final DeviceService deviceService;
    private final ProfileFaultService profileFaultService;
    private final FaultTypeService faultTypeService;
    private final DiagnosticMethodService diagnosticMethodService;

    @GetMapping
    public String listProfiles(Model model) {
        model.addAttribute("profiles", profileService.findAll());
        return "profiles/list";
    }
    
    @PostMapping("/{id}/update-fault-status")
    @ResponseBody
    public Map<String, Object> updateFaultStatus(@PathVariable Long id,
                                               @RequestParam Long deviceId,
                                               @RequestParam Long faultId,
                                               @RequestParam boolean covered) {
        Map<String, Object> response = new HashMap<>();
        try {
            profileFaultService.updateFaultStatus(id, deviceId, faultId, covered);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/{id}")
    public String viewProfile(@PathVariable Long id, Model model) {
        Profile profile = profileService.findByIdWithDetails(id);
        
        // Создаем список устройств с их неисправностями
        List<DeviceWithFaultsDto> devicesWithFaults = new ArrayList<>();
        
        // Для каждого устройства в профиле
        for (Device device : profile.getDevices()) {
            DeviceWithFaultsDto deviceWithFaults = new DeviceWithFaultsDto();
            deviceWithFaults.setDevice(device);
            
            if (device.getCategory() != null) {
                // Получаем все неисправности для категории устройства
                List<FaultTypeDto> deviceFaults = faultTypeService.findByDeviceCategoryId(device.getCategory().getId());
                
                // Получаем привязанные неисправности профиля для этого устройства
                List<ProfileFaultDto> profileFaults = profileFaultService.findByProfileIdAndDeviceCategoryId(
                    id, device.getCategory().getId());
                
                // Создаем мапу для быстрого доступа к привязанным неисправностям
                Map<Long, ProfileFaultDto> profileFaultsMap = profileFaults.stream()
                    .collect(Collectors.toMap(
                        ProfileFaultDto::getFaultTypeId,
                        pf -> pf
                    ));
                
                // Объединяем данные
                List<FaultWithStatusDto> faults = deviceFaults.stream()
                    .map(ft -> {
                        FaultWithStatusDto dto = new FaultWithStatusDto();
                        dto.setFaultType(ft);
                        if (profileFaultsMap.containsKey(ft.getId())) {
                            ProfileFaultDto pf = profileFaultsMap.get(ft.getId());
                            dto.setCovered(pf.getCovered());
                            dto.setNotes(pf.getNotes());
                        } else {
                            dto.setCovered(false);
                            dto.setNotes("");
                        }
                        return dto;
                    })
                    .sorted(Comparator.comparing(f -> f.getFaultType().getCoverageRequirement()))
                    .collect(Collectors.toList());
                
                deviceWithFaults.setFaults(faults);
                
                // Get sorted diagnostic methods for this device and architecture
                List<DiagnosticMethod> diagnosticMethods = diagnosticMethodService
                    .findByDeviceAndArchitecture(device, profile.getArchitectureType());
                deviceWithFaults.setDiagnosticMethods(diagnosticMethods);
            } else {
                deviceWithFaults.setFaults(Collections.emptyList());
                deviceWithFaults.setDiagnosticMethods(Collections.emptyList());
            }
            
            devicesWithFaults.add(deviceWithFaults);
        }
        
        model.addAttribute("profile", profile);
        model.addAttribute("devicesWithFaults", devicesWithFaults);
        return "profiles/view";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("profile", new Profile());
        model.addAttribute("allDevices", deviceService.findAll());
        return "profiles/form";
    }

    @PostMapping
    public String createProfile(@ModelAttribute Profile profile,
                              @RequestParam(value = "deviceIds", required = false) Set<Long> deviceIds,
                              @RequestParam Map<String, String> allParams,
                              RedirectAttributes redirectAttributes) {
        try {
            // Extract device methods from request parameters
            Map<Long, Set<Long>> deviceMethods = new HashMap<>();
            if (deviceIds != null) {
                for (Long deviceId : deviceIds) {
                    String[] methodIds = allParams.get("deviceMethods[" + deviceId + "]") != null ? 
                            allParams.get("deviceMethods[" + deviceId + "]").split(",") : new String[0];
                    Set<Long> methodIdSet = Arrays.stream(methodIds)
                            .filter(id -> !id.isEmpty())
                            .map(Long::parseLong)
                            .collect(Collectors.toSet());
                    if (!methodIdSet.isEmpty()) {
                        deviceMethods.put(deviceId, methodIdSet);
                    }
                }
            }
            
            profileService.create(profile, 
                               deviceIds != null ? deviceIds : new HashSet<>(), 
                               deviceMethods);
            redirectAttributes.addFlashAttribute("successMessage", "Profile created successfully");
            return "redirect:/profiles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating profile: " + e.getMessage());
            return "redirect:/profiles/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Profile profile = profileService.findById(id);
        model.addAttribute("profile", profile);
        model.addAttribute("allDevices", deviceService.findAll());
        model.addAttribute("selectedDeviceIds", profile.getDevices().stream()
                .map(device -> device.getId())
                .toList());
        return "profiles/form";
    }

    @PutMapping("/{id}")
    @PostMapping("/{id}")
    public String updateProfile(@PathVariable Long id,
                              @ModelAttribute Profile profile,
                              @RequestParam(value = "deviceIds", required = false) Set<Long> deviceIds,
                              @RequestParam Map<String, String> allParams,
                              RedirectAttributes redirectAttributes) {
        try {
            // Extract device methods from request parameters
            Map<Long, Set<Long>> deviceMethods = new HashMap<>();
            if (deviceIds != null) {
                for (Long deviceId : deviceIds) {
                    String[] methodIds = allParams.get("deviceMethods[" + deviceId + "]") != null ? 
                            allParams.get("deviceMethods[" + deviceId + "]").split(",") : new String[0];
                    Set<Long> methodIdSet = Arrays.stream(methodIds)
                            .filter(methodId -> !methodId.isEmpty())
                            .map(Long::parseLong)
                            .collect(Collectors.toSet());
                    if (!methodIdSet.isEmpty()) {
                        deviceMethods.put(deviceId, methodIdSet);
                    }
                }
            }
            
            profileService.update(id, profile, 
                               deviceIds != null ? deviceIds : new HashSet<>(),
                               deviceMethods);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
            return "redirect:/profiles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            return "redirect:/profiles/" + id + "/edit";
        }
    }

    @DeleteMapping("/{id}")
    @PostMapping("/{id}/delete")
    public String deleteProfile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            profileService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Profile deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting profile: " + e.getMessage());
        }
        return "redirect:/profiles";
    }
}
