package com.quizapp.service;

import com.quizapp.dto.OptionDto;
import com.quizapp.dto.QuestionDto;
import com.quizapp.dto.QuizDto;
import com.quizapp.model.Option;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @InjectMocks private QuizService quizService;

    private QuizDto quizDto;

    @BeforeEach
    void setUp() {
        OptionDto opt1 = new OptionDto();
        opt1.setText("Paris");
        opt1.setCorrect(true);

        OptionDto opt2 = new OptionDto();
        opt2.setText("London");
        opt2.setCorrect(false);

        QuestionDto question = new QuestionDto();
        question.setText("Capital of France?");
        question.setOptions(List.of(opt1, opt2));

        quizDto = new QuizDto();
        quizDto.setTitle("Geography Quiz");
        quizDto.setDescription("Test your geography");
        quizDto.setTimeLimitSeconds(120);
        quizDto.setQuestions(List.of(question));
    }

    @Test
    void createQuiz_savesQuizWithCreatorAndGeneratedIds() {
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));

        Quiz result = quizService.createQuiz(quizDto, "admin1");

        ArgumentCaptor<Quiz> captor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizRepository).save(captor.capture());
        Quiz saved = captor.getValue();

        assertEquals("Geography Quiz", saved.getTitle());
        assertEquals("admin1", saved.getCreatedByUsername());
        assertEquals(1, saved.getQuestions().size());
        assertNotNull(saved.getQuestions().get(0).getId(), "question id should be auto-generated");
        assertNotNull(saved.getQuestions().get(0).getOptions().get(0).getId(), "option id should be auto-generated");
    }

    @Test
    void findById_throwsWhenQuizDoesNotExist() {
        when(quizRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> quizService.findById("missing"));
    }

    @Test
    void getQuestionsForParticipant_stripsCorrectFlag() {
        Option correctOption = new Option("o1", "Paris", true);
        Option wrongOption = new Option("o2", "London", false);
        Question question = new Question("q1", "Capital of France?", List.of(correctOption, wrongOption));

        Quiz quiz = new Quiz();
        quiz.setQuestions(List.of(question));

        List<Question> sanitized = quizService.getQuestionsForParticipant(quiz);

        assertEquals(1, sanitized.size());
        sanitized.get(0).getOptions().forEach(o ->
                assertFalse(o.isCorrect(), "correct flag must never reach the participant view"));
    }

    @Test
    void deleteQuiz_delegatesToRepository() {
        quizService.deleteQuiz("quiz1");
        verify(quizRepository).deleteById("quiz1");
    }
}
