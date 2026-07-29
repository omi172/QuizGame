package com.quizapp.dto;

import java.util.List;

public class SubmitAttemptRequest {

    private String quizId;
    private List<AnswerSubmission> answers;

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public List<AnswerSubmission> getAnswers() { return answers; }
    public void setAnswers(List<AnswerSubmission> answers) { this.answers = answers; }
}
