package com.mindshare.quiz;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class QuizQuestionBuilderView extends VBox {

    private final Label headerLabel = new Label();
    private final TextField questionField = new TextField();
    private final VBox optionsBox = new VBox(8);
    private final ComboBox<String> correctOptionBox = new ComboBox<>();
    private final Spinner<Integer> marksSpinner = new Spinner<>();
    private final Button addOptionButton = new Button("+ Add Option");
    private final Button removeQuestionButton = new Button("Remove");
    private final QuizQuestionDraft draft;
    private final Runnable removeSelfAction;

    public QuizQuestionBuilderView(int questionNumber, QuizQuestionDraft draft, Runnable removeSelfAction) {
        this.draft = draft;
        this.removeSelfAction = removeSelfAction;

        getStyleClass().add("quiz-question-card");
        setSpacing(12);
        setStyle("-fx-background-color: linear-gradient(to bottom, #f7f9fc, #e9eef7);"
                + "-fx-background-radius: 18;"
                + "-fx-padding: 18;"
                + "-fx-border-color: rgba(120,130,150,0.18);"
                + "-fx-border-radius: 18;");

        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerLabel.setText("Question " + questionNumber);
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        removeQuestionButton.setOnAction(e -> removeSelfAction.run());
        headerRow.getChildren().addAll(headerLabel, new HBox(), removeQuestionButton);
        HBox.setHgrow(headerRow.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

        questionField.setPromptText("Enter question text");
        questionField.textProperty().addListener((obs, oldValue, newValue) -> draft.setQuestionText(newValue));

        correctOptionBox.setPromptText("Correct option");
        correctOptionBox.setItems(FXCollections.observableArrayList());
        correctOptionBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                draft.setCorrectOptionIndex(correctOptionBox.getItems().indexOf(newValue));
            }
        });

        marksSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        marksSpinner.setEditable(true);
        marksSpinner.valueProperty().addListener((obs, oldValue, newValue) -> draft.setMarks(newValue));

        addOptionButton.setOnAction(e -> addOptionField(""));

        HBox settingsRow = new HBox(14);
        settingsRow.setAlignment(Pos.CENTER_LEFT);
        Label marksLabel = new Label("Marks for this question:");
        marksLabel.setStyle("-fx-font-weight: bold;");
        settingsRow.getChildren().addAll(addOptionButton, marksLabel, marksSpinner);

        getChildren().addAll(headerRow, questionField, optionsBox, correctOptionBox, settingsRow);

        draft.ensureMinimumOptions(2);
        rebuildOptions();
        syncCorrectOptions();
    }

    private void addOptionField(String initialValue) {
        draft.getOptions().add(initialValue);
        rebuildOptions();
        syncCorrectOptions();
    }

    private void removeOptionField(int index) {
        if (draft.getOptions().size() <= 2) {
            return;
        }
        draft.getOptions().remove(index);
        if (draft.getCorrectOptionIndex() >= draft.getOptions().size()) {
            draft.setCorrectOptionIndex(Math.max(0, draft.getOptions().size() - 1));
        }
        rebuildOptions();
        syncCorrectOptions();
    }

    private void rebuildOptions() {
        optionsBox.getChildren().clear();
        correctOptionBox.getItems().clear();

        List<String> options = draft.getOptions();
        for (int i = 0; i < options.size(); i++) {
            int optionIndex = i;
            TextField optionField = new TextField(options.get(i));
            optionField.setPromptText("Option text");
            optionField.textProperty().addListener((obs, oldValue, newValue) -> options.set(optionIndex, newValue));

            RadioButton correctRadio = new RadioButton();
            ToggleGroup group = new ToggleGroup();
            correctRadio.setToggleGroup(group);
            correctRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    draft.setCorrectOptionIndex(optionIndex);
                }
            });
            if (draft.getCorrectOptionIndex() == optionIndex) {
                correctRadio.setSelected(true);
            }

            Button deleteOptionButton = new Button("X");
            deleteOptionButton.setOnAction(e -> removeOptionField(optionIndex));

            HBox optionRow = new HBox(10, correctRadio, optionField, deleteOptionButton);
            optionRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(optionField, javafx.scene.layout.Priority.ALWAYS);
            optionsBox.getChildren().add(optionRow);
            correctOptionBox.getItems().add("Option " + (i + 1));
        }

        correctOptionBox.setValue(correctOptionBox.getItems().isEmpty()
                ? null
                : correctOptionBox.getItems().get(Math.min(draft.getCorrectOptionIndex(), correctOptionBox.getItems().size() - 1)));
    }

    private void syncCorrectOptions() {
        correctOptionBox.setItems(FXCollections.observableArrayList(
                draft.getOptions().stream().map(option -> option.isBlank() ? "Untitled option" : option).toList()
        ));
        if (!correctOptionBox.getItems().isEmpty()) {
            int safeIndex = Math.min(draft.getCorrectOptionIndex(), correctOptionBox.getItems().size() - 1);
            correctOptionBox.setValue(correctOptionBox.getItems().get(safeIndex));
        }
    }

    public QuizQuestionDraft getDraft() {
        return draft;
    }

    public boolean isValid() {
        if (draft.getQuestionText() == null || draft.getQuestionText().isBlank()) {
            return false;
        }
        if (draft.getOptions().size() < 2) {
            return false;
        }
        if (draft.getCorrectOptionIndex() < 0 || draft.getCorrectOptionIndex() >= draft.getOptions().size()) {
            return false;
        }
        return draft.getOptions().stream().noneMatch(option -> option == null || option.isBlank());
    }
}
