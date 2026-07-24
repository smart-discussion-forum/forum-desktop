package com.mindshare.discussion;

import com.mindshare.discussion.controller.TopicDetailController;
import com.mindshare.discussion.model.Post;
import com.mindshare.discussion.model.Topic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

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
        sb.append("Topic: ").append(topic.getTitle()).append("\n\n");
        for (Post p : posts) {
            sb.append("[").append(p.getCreatedAt()).append("] ")
                    .append(p.getAuthorName()).append(": ").append(p.getContent()).append("\n");
        }
        summaryArea.setText(sb.toString());
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(40, 700);
                for (String line : summaryArea.getText().split("\\R")) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -16);
                }
                contentStream.endText();
            }

            try (FileOutputStream outputStream = new FileOutputStream(topic.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_export.pdf")) {
                document.save(outputStream);
            }
            System.out.println("Exported discussion as PDF.");
        } catch (Exception e) {
            e.printStackTrace();
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
}
