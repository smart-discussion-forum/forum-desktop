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

import java.io.FileWriter;
import java.util.List;

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
        // TODO: replace with proper PDF generation (e.g. using a PDF library) - text file for now to prove the export flow works
        try (FileWriter writer = new FileWriter(topic.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_export.txt")) {
            writer.write(summaryArea.getText());
            System.out.println("Exported to text file (placeholder for PDF).");
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
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    }
