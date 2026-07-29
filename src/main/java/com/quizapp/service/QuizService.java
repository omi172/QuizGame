package com.quizapp.service;

import com.quizapp.dto.OptionDto;
import com.quizapp.dto.QuestionDto;
import com.quizapp.dto.QuizDto;
import com.quizapp.model.Option;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    public List<Quiz> findAll() {
        return quizRepository.findAll();
    }

    public Quiz findById(String id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found: " + id));
    }

    public Quiz createQuiz(QuizDto dto, String createdByUsername) {
        Quiz quiz = new Quiz();
        quiz.setCreatedByUsername(createdByUsername);
        applyDtoToQuiz(dto, quiz);
        return quizRepository.save(quiz);
    }

    public Quiz updateQuiz(String id, QuizDto dto) {
        Quiz quiz = findById(id);
        applyDtoToQuiz(dto, quiz);
        return quizRepository.save(quiz);
    }

    public void deleteQuiz(String id) {
        quizRepository.deleteById(id);
    }

    private void applyDtoToQuiz(QuizDto dto, Quiz quiz) {
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTimeLimitSeconds(dto.getTimeLimitSeconds());

        List<Question> questions = new ArrayList<>();
        for (QuestionDto qDto : dto.getQuestions()) {
            String questionId = (qDto.getId() != null && !qDto.getId().isBlank())
                    ? qDto.getId() : UUID.randomUUID().toString();

            List<Option> options = qDto.getOptions().stream()
                    .map(this::toOption)
                    .collect(Collectors.toList());

            questions.add(new Question(questionId, qDto.getText(), options));
        }
        quiz.setQuestions(questions);
    }

    private Option toOption(OptionDto dto) {
        String optionId = (dto.getId() != null && !dto.getId().isBlank())
                ? dto.getId() : UUID.randomUUID().toString();
        return new Option(optionId, dto.getText(), dto.isCorrect());
    }

    public List<Question> getQuestionsForParticipant(Quiz quiz) {
        List<Question> sanitized = new ArrayList<>();
        for (Question q : quiz.getQuestions()) {
            List<Option> options = q.getOptions().stream()
                    .map(o -> new Option(o.getId(), o.getText(), false))
                    .collect(Collectors.toList());
            sanitized.add(new Question(q.getId(), q.getText(), options));
        }
        return sanitized;
    }
}
