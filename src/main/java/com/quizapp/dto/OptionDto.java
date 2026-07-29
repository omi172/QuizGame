package com.quizapp.dto;

import jakarta.validation.constraints.NotBlank;

public class OptionDto {

    private String id;

    @NotBlank(message = "Option text is required")
    private String text;

    private boolean correct;

    public OptionDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
