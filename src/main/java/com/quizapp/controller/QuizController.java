package com.quizapp.controller;

import com.quizapp.dto.SubmitAttemptRequest;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizAttempt;
import com.quizapp.model.User;
import com.quizapp.service.QuizAttemptService;
import com.quizapp.service.QuizService;
import com.quizapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @Autowired private QuizService quizService;
    @Autowired private QuizAttemptService quizAttemptService;
    @Autowired private UserService userService;

    @GetMapping("/list")
    public String listQuizzes(Model model) {
        model.addAttribute("quizzes", quizService.findAll());
        return "quiz/list";
    }

    @GetMapping("/{id}/take")
    public String takeQuiz(@PathVariable String id, Model model) {
        Quiz quiz = quizService.findById(id);
        model.addAttribute("quiz", quiz);
        // Participant-facing view never receives which option is correct.
        model.addAttribute("questions", quizService.getQuestionsForParticipant(quiz));
        return "quiz/take";
    }

    /**
     * Called when the participant finishes or the timer runs out.
     * Grades the submission server-side and redirects to the result page -
     * the client never sees the answer key, only the final score.
     */
    @PostMapping("/{id}/submit")
    @ResponseBody
    public String submitQuiz(@PathVariable String id, @RequestBody SubmitAttemptRequest request,
                              Authentication authentication) {
        request.setQuizId(id);
        User user = userService.findByUsername(authentication.getName());
        QuizAttempt attempt = quizAttemptService.gradeAndSave(request, user);
        return "/quiz/result/" + attempt.getId();
    }

    @GetMapping("/result/{attemptId}")
    public String showResult(@PathVariable String attemptId, Model model) {
        QuizAttempt attempt = quizAttemptService.findById(attemptId);
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", quizService.findById(attempt.getQuizId()));
        return "quiz/result";
    }

    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("attempts", quizAttemptService.findByUser(user.getId()));
        return "quiz/history";
    }
}
