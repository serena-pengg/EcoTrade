package com.example.Secondhand.controller;

import com.example.Secondhand.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/contact/submit")
    public String submitContactForm(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {
        
        try {
            contactService.submitContactForm(email, subject, message);
            redirectAttributes.addFlashAttribute("success", "Your message has been sent successfully!");
            return "redirect:/contact";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send message. Please try again.");
            return "redirect:/contact";
        }
    }
} 