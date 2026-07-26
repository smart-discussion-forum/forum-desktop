package com.mindshare.discussion.controller;

import com.mindshare.discussion.model.Post;
import com.mindshare.discussion.model.Topic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class ExportDiscussionController {
    @FXML private TextArea summaryArea;
    @FXML private Button exportButton;
    @FXML private Button closeButton;

    private Topic topic;
    private List<Post> posts;

    public void setDiscussion(Topic topic, List<Post> posts) {
        this.topic = topic;
        this.posts = posts;

        StringBuilder sb = new StringBuilder();
        sb.append("Topic: ").append(topic != null ? topic.getTitle() : "Discussion").append("\n\n");

        if (posts != null) {
            for (Post p : posts) {
                sb.append("[").append(p.getCreatedAt() != null ? p.getCreatedAt() : "").append("] ")
                        .append(p.getAuthorName() != null ? p.getAuthorName() : "User").append(": ")
                        .append(p.getContent() != null ? p.getContent() : "").append("\n");
            }
        }
        summaryArea.setText(sb.toString());
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        if (summaryArea.getText() == null || summaryArea.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Export Warning", "There is no content to export.");
            return;
        }

        // 1. Let user select save destination
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Discussion as PDF");

        String defaultFileName = (topic != null && topic.getTitle() != null)
                ? topic.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + "_export.pdf"
                : "discussion_export.pdf";

        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File saveFile = fileChooser.showSaveDialog(stage);

        if (saveFile == null) {
            return; // User canceled the file picker
        }

        // 2. Generate PDF using Apache PDFBox
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.setLeading(14.5f); // Set line spacing
                contentStream.newLineAtOffset(40, 720);

                String[] lines = summaryArea.getText().split("\\R");
                for (String line : lines) {
                    // PDFBox showText cannot process tab characters or newlines
                    String sanitizedLine = line.replace("\t", "    ").replaceAll("[\\r\\n]", "");
                    contentStream.showText(sanitizedLine);
                    contentStream.newLine();
                }
                contentStream.endText();
            }

            try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                document.save(outputStream);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Discussion exported successfully to:\n" + saveFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to generate PDF: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/discussion/TopicDetailView.fxml"));
            Parent root = loader.load();
            TopicDetailController controller = loader.getController();
            controller.setTopic(topic);
            Stage stage = (Stage) closeButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}