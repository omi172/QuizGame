package com.quizapp.dto;

public class AnswerSubmission {

    private String questionId;
    private String selectedOptionId;

    public AnswerSubmission() {}

    public AnswerSubmission(String questionId, String selectedOptionId) {
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
    }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(String selectedOptionId) { this.selectedOptionId = selectedOptionId; }
}
