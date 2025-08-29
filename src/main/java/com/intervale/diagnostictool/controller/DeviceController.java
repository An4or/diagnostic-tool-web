package com.intervale.diagnostictool.controller;

import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.service.DeviceCategoryService;
import com.intervale.diagnostictool.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceCategoryService categoryService;

    @GetMapping
    public String listDevices(Model model) {
        model.addAttribute("devices", deviceService.findAll());
        return "devices/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("device", new Device());
        model.addAttribute("categories", categoryService.findAll());
        return "devices/form";
    }

    @PostMapping
    public String createDevice(@ModelAttribute Device device,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             RedirectAttributes redirectAttributes) {
        try {
            deviceService.create(device, categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Device created successfully");
            return "redirect:/devices";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating device: " + e.getMessage());
            return "redirect:/devices/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Device device = deviceService.findById(id);
        model.addAttribute("device", device);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryId", 
            device.getCategory() != null ? device.getCategory().getId() : null);
        return "devices/form";
    }

    @PostMapping("/{id}")
    public String updateDevice(@PathVariable Long id,
                             @ModelAttribute Device device,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             RedirectAttributes redirectAttributes) {
        try {
            deviceService.update(id, device, categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Device updated successfully");
            return "redirect:/devices";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating device: " + e.getMessage());
            return "redirect:/devices/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteDevice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            deviceService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Device deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting device: " + e.getMessage());
        }
        return "redirect:/devices";
    }
}
