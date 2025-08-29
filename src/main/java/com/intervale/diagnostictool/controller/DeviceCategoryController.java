package com.intervale.diagnostictool.controller;

import com.intervale.diagnostictool.model.DeviceCategory;
import com.intervale.diagnostictool.service.DeviceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/device-categories")
@RequiredArgsConstructor
public class DeviceCategoryController {

    private final DeviceCategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "device-categories/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new DeviceCategory());
        return "device-categories/form";
    }

    @PostMapping
    public String createCategory(@ModelAttribute DeviceCategory category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.create(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully");
            return "redirect:/device-categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating category: " + e.getMessage());
            return "redirect:/device-categories/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "device-categories/form";
    }

    @PostMapping("/{id}")
    public String updateCategory(@PathVariable Long id, 
                               @ModelAttribute DeviceCategory category,
                               RedirectAttributes redirectAttributes) {
        try {
            categoryService.update(id, category);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully");
            return "redirect:/device-categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating category: " + e.getMessage());
            return "redirect:/device-categories/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting category: " + e.getMessage());
        }
        return "redirect:/device-categories";
    }
}
