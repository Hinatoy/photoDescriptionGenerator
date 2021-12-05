package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import com.amazonaws.services.polly.model.AmazonPollyException;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.util.IOUtils;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class DetectLabels {
	public String finalDescription(String photoAddress) throws Exception {
		AnalysisPhoto analysis = new AnalysisPhoto();
        String photo = photoAddress;
        String finalText = "";
        
        ByteBuffer imageBytes;
        try (InputStream inputStream = new FileInputStream(new File(photo))) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        }

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image()
                        .withBytes(imageBytes))
                .withMaxLabels(50)
                .withMinConfidence(55F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();
            String sceneryLabels = "", faceLabels = "", clothingLabels = "";
            sceneryLabels = analysis.describeScenery(labels);
            
            if(analysis.requiresFacialAnalysis) {
                try {
                	DetectFacesRequest request2 = new DetectFacesRequest()
                        .withImage(new Image()
                           .withBytes(imageBytes))
                        .withAttributes(Attribute.ALL);
                
	                DetectFacesResult facialAnalysis = rekognitionClient.detectFaces(request2);
	                List <FaceDetail> faceDetails = facialAnalysis.getFaceDetails();
	                
	                faceLabels = analysis.describeFaces(faceDetails);
                } catch (AmazonRekognitionException e) {
                    e.printStackTrace();
                }
            }
            
            if(analysis.requiresClothingAnalysis) {
	            clothingLabels = analysis.describeClothing(labels);
            }
            
            finalText = sceneryLabels + faceLabels + clothingLabels;
            finalText = "<speak><prosody rate='70%'>" + finalText + "</prosody></speak>";
            System.out.println(finalText);
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
        
        return finalText;
	}
	
	public void playFinalDescription(String finalDescription) throws JavaLayerException, IOException {
        try {
        	Polly pollyDemo = new Polly();
	        InputStream speechStream = pollyDemo.synthesize(finalDescription, OutputFormat.Mp3);
	        
	        AdvancedPlayer player = new AdvancedPlayer(speechStream, 
	        		javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
	        
	        player.play();
        } catch (AmazonPollyException e) {
        	e.printStackTrace();
        }
		
	}
}