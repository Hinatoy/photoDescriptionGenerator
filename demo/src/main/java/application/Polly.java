package application;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.Voice;

public class Polly {
	private final AmazonPolly polly;
	private final Voice voice;
	
	private static final String EMMA="Emma";
	
	public String text = "";
	
	public Polly() {
		polly = AmazonPollyClientBuilder.standard()
				.withRegion(Regions.US_EAST_1).build();
		DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
		
		DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
		voice = describeVoicesResult.getVoices().stream().filter(p -> p.getName().equals(EMMA)).findFirst().get();
	}
	
	public InputStream synthesize(String text, OutputFormat format) throws IOException {
		SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest().withTextType(TextType.Ssml).withText(text).withVoiceId(voice.getId())
				.withOutputFormat(format);
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
		
		return synthRes.getAudioStream();
	}
}
