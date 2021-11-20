package com.amazonaws.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Instance;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.Parent;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;

public class DetectLabels {

    public static void main(String[] args) throws Exception {

        String photo = "input1.jpg";
        
        ByteBuffer imageBytes;
        try (InputStream inputStream = new FileInputStream(new File(photo))) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        }


        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image()
                        .withBytes(imageBytes))
                .withMaxLabels(50)
                .withMinConfidence(50F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();

            System.out.println("Detected labels for " + photo + "\n");
            for (Label label : labels) { 
            	if(label.getName().equals("Jeans")) {
	                System.out.println("Label: " + label.getName());
	                System.out.println("Confidence: " + label.getConfidence().toString() + "\n");
	
	                List<Instance> instances = label.getInstances();
	                System.out.println("Instances of " + label.getName());
	                if (instances.isEmpty()) {
	                    System.out.println("  " + "None");
	                } else {
	                    for (Instance instance : instances) {
	                        System.out.println("  Confidence: " + instance.getConfidence().toString());
	                        System.out.println("  Bounding box: " + instance.getBoundingBox().toString());
	                    }
	                }
	                System.out.println("Parent labels for " + label.getName() + ":");
	                List<Parent> parents = label.getParents();
	                if (parents.isEmpty()) {
	                    System.out.println("  None");
	                } else {
	                    for (Parent parent : parents) {
	                        System.out.println("  " + parent.getName());
	                    }
	                }
	                System.out.println("--------------------");
	                System.out.println();
            	}
            }
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
    }
}