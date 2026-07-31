package com.quizapp.service;

import com.quizapp.dto.AnswerSubmission;
import com.quizapp.dto.SubmitAttemptRequest;
import com.quizapp.model.*;
import com.quizapp.repository.QuizAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceTest {

    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private QuizService quizService;
    @InjectMocks private QuizAttemptService quizAttemptService;

    private Quiz quiz;
    private User user;

    @BeforeEach
    void setUp() {
        Question q1 = new Question("q1", "Capital of France?", List.of(
                new Option("o1", "Paris", true),
                new Option("o2", "London", false)
        ));
        Question q2 = new Question("q2", "2 + 2 = ?", List.of(
                new Option("o1", "3", false),
                new Option("o2", "4", true)
        ));

        quiz = new Quiz();
        quiz.setId("quiz1");
        quiz.setQuestions(List.of(q1, q2));

        user = new User("alice", "alice@example.com", "hashed", Role.PARTICIPANT);
        user.setId("user1");
    }

    @Test
    void gradeAndSave_scoresAllCorrectAnswers() {
        when(quizService.findById("quiz1")).thenReturn(quiz);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitAttemptRequest request = new SubmitAttemptRequest();
        request.setQuizId("quiz1");
        request.setAnswers(List.of(
                new AnswerSubmission("q1", "o1"), // correct
                new AnswerSubmission("q2", "o2")  // correct
        ));

        QuizAttempt attempt = quizAttemptService.gradeAndSave(request, user);

        assertEquals(2, attempt.getScore());
        assertEquals(2, attempt.getTotalQuestions());
        assertEquals("user1", attempt.getUserId());
        assertEquals("quiz1", attempt.getQuizId());
    }

    @Test
    void gradeAndSave_scoresPartiallyCorrectAnswers() {
        when(quizService.findById("quiz1")).thenReturn(quiz);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitAttemptRequest request = new SubmitAttemptRequest();
        request.setQuizId("quiz1");
        request.setAnswers(List.of(
                new AnswerSubmission("q1", "o2"), // wrong
                new AnswerSubmission("q2", "o2")  // correct
        ));

        QuizAttempt attempt = quizAttemptService.gradeAndSave(request, user);

        assertEquals(1, attempt.getScore());
    }

    @Test
    void gradeAndSave_persistsAnswersRegardlessOfCorrectness() {
        when(quizService.findById("quiz1")).thenReturn(quiz);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitAttemptRequest request = new SubmitAttemptRequest();
        request.setQuizId("quiz1");
        request.setAnswers(List.of(new AnswerSubmission("q1", "o2")));

        ArgumentCaptor<QuizAttempt> captor = ArgumentCaptor.forClass(QuizAttempt.class);
        quizAttemptService.gradeAndSave(request, user);
        verify(quizAttemptRepository).save(captor.capture());

        assertEquals(1, captor.getValue().getAnswers().size());
        assertEquals("o2", captor.getValue().getAnswers().get(0).getSelectedOptionId());
    }

    @Test
    void gradeAndSave_unansweredOrUnknownQuestionCountsAsWrong() {
        when(quizService.findById("quiz1")).thenReturn(quiz);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitAttemptRequest request = new SubmitAttemptRequest();
        request.setQuizId("quiz1");
        request.setAnswers(List.of(new AnswerSubmission("q1", null)));

        QuizAttempt attempt = quizAttemptService.gradeAndSave(request, user);

        assertEquals(0, attempt.getScore());
    }
}
