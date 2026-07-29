package com.quizapp.controller;

import com.quizapp.dto.OptionDto;
import com.quizapp.dto.QuestionDto;
import com.quizapp.dto.QuizDto;
import com.quizapp.model.Quiz;
import com.quizapp.service.QuizAttemptService;
import com.quizapp.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminQuizController {

    @Autowired private QuizService quizService;
    @Autowired private QuizAttemptService quizAttemptService;

    @GetMapping("/quizzes")
    public String listQuizzes(Model model) {
        model.addAttribute("quizzes", quizService.findAll());
        return "admin/quiz-list";
    }

    @GetMapping("/quizzes/new")
    public String newQuizForm(Model model) {
        QuizDto dto = new QuizDto();
        QuestionDto q = new QuestionDto();
        q.setOptions(new ArrayList<>(List.of(new OptionDto(), new OptionDto())));
        dto.setQuestions(new ArrayList<>(List.of(q)));
        model.addAttribute("quizDto", dto);
        return "admin/quiz-form";
    }

    @PostMapping("/quizzes")
    public String createQuiz(@Valid @ModelAttribute("quizDto") QuizDto dto, BindingResult result,
                              Authentication authentication, Model model) {
        if (result.hasErrors()) {
            return "admin/quiz-form";
        }
        if (!hasExactlyOneCorrectOptionPerQuestion(dto)) {
            model.addAttribute("errorMessage", "Each question needs exactly one correct option marked.");
            return "admin/quiz-form";
        }
        quizService.createQuiz(dto, authentication.getName());
        return "redirect:/admin/quizzes";
    }

    @GetMapping("/quizzes/{id}/edit")
    public String editQuizForm(@PathVariable String id, Model model) {
        Quiz quiz = quizService.findById(id);
        model.addAttribute("quizDto", toDto(quiz));
        return "admin/quiz-form";
    }

    @PostMapping("/quizzes/{id}")
    public String updateQuiz(@PathVariable String id, @Valid @ModelAttribute("quizDto") QuizDto dto,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "admin/quiz-form";
        }
        if (!hasExactlyOneCorrectOptionPerQuestion(dto)) {
            model.addAttribute("errorMessage", "Each question needs exactly one correct option marked.");
            return "admin/quiz-form";
        }
        quizService.updateQuiz(id, dto);
        return "redirect:/admin/quizzes";
    }

    @PostMapping("/quizzes/{id}/delete")
    public String deleteQuiz(@PathVariable String id) {
        quizService.deleteQuiz(id);
        return "redirect:/admin/quizzes";
    }

    @GetMapping("/quizzes/{id}/results")
    public String viewResults(@PathVariable String id, Model model) {
        model.addAttribute("quiz", quizService.findById(id));
        model.addAttribute("attempts", quizAttemptService.findByQuiz(id));
        return "admin/quiz-results";
    }

    private boolean hasExactlyOneCorrectOptionPerQuestion(QuizDto dto) {
        for (QuestionDto q : dto.getQuestions()) {
            long correctCount = q.getOptions().stream().filter(OptionDto::isCorrect).count();
            if (correctCount != 1) return false;
        }
        return true;
    }

    private QuizDto toDto(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTimeLimitSeconds(quiz.getTimeLimitSeconds());
        dto.setQuestions(quiz.getQuestions().stream().map(q -> {
            QuestionDto qDto = new QuestionDto();
            qDto.setId(q.getId());
            qDto.setText(q.getText());
            qDto.setOptions(q.getOptions().stream().map(o -> {
                OptionDto oDto = new OptionDto();
                oDto.setId(o.getId());
                oDto.setText(o.getText());
                oDto.setCorrect(o.isCorrect());
                return oDto;
            }).collect(Collectors.toList()));
            return qDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
