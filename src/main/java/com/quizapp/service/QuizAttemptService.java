package com.quizapp.service;

import com.quizapp.dto.AnswerSubmission;
import com.quizapp.dto.SubmitAttemptRequest;
import com.quizapp.model.Answer;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizAttempt;
import com.quizapp.model.User;
import com.quizapp.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizService quizService;

    public QuizAttempt gradeAndSave(SubmitAttemptRequest request, User user) {
        Quiz quiz = quizService.findById(request.getQuizId());

        Map<String, String> correctAnswers = quiz.getQuestions().stream()
                .collect(Collectors.toMap(q -> q.getId(), q -> q.getCorrectOptionId()));

        int score = 0;
        List<Answer> answers = new ArrayList<>();

        for (AnswerSubmission submission : request.getAnswers()) {
            answers.add(new Answer(submission.getQuestionId(), submission.getSelectedOptionId()));

            String correctOptionId = correctAnswers.get(submission.getQuestionId());
            if (correctOptionId != null && correctOptionId.equals(submission.getSelectedOptionId())) {
                score++;
            }
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(user.getId());
        attempt.setUsername(user.getUsername());
        attempt.setQuizId(quiz.getId());
        attempt.setAnswers(answers);
        attempt.setScore(score);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setSubmittedAt(Instant.now());

        return quizAttemptRepository.save(attempt);
    }

    public List<QuizAttempt> findByUser(String userId) {
        return quizAttemptRepository.findByUserId(userId);
    }

    public List<QuizAttempt> findByQuiz(String quizId) {
        return quizAttemptRepository.findByQuizId(quizId);
    }

    public QuizAttempt findById(String id) {
        return quizAttemptRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Attempt not found: " + id));
    }
}
