package application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.rekognition.model.Emotion;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.Parent;

public class Analysis {
	public boolean requiresClothingAnalysis = false; // if exactly one person detected
	public boolean requiresFacialAnalysis = false; // if human / person was detected
	public String gender = "";
	
	public String describeScenery(List<Label> labels) {
    	List<Label> sceneryLabels = new ArrayList<Label>();
    	boolean skip = false;
    	for(Label label : labels) {
    		// exclude labels with the following names (too vague)
    		skip = false;
    		
    		if(label.getName().equals("Person") || label.getName().equals("Human")) {
    			requiresFacialAnalysis = true;
    		}
    		
    		if(label.getName().equals("Panoramic") || 
    				label.getName().equals("Urban") ||
    				label.getName().equals("Scenery") ||
    				label.getName().equals("Landscape") ||
    				label.getName().equals("Weather") ||
    				label.getName().equals("Tropical") ||
    				label.getName().equals("Accessories") ||
    				label.getName().equals("Accessory") ||
    				label.getName().equals("Clothing") ||
    				label.getName().equals("Human") ||
    				label.getName().equals("Person") ||
    				label.getName().equals("People") ||
    				label.getName().equals("Female") ||
    				label.getName().equals("Male") ||
    				label.getName().equals("Girl") ||
    				label.getName().equals("Boy") ||
    				label.getName().equals("Man") ||
    				label.getName().equals("Woman") ||
    				label.getName().equals("Face") ||
    				label.getName().equals("Apparel") ||
    				label.getName().equals("Bag")) {
    			continue;
    		} 
    		// case "reject an item":
    		// case 1: do not add labels that are parents of items already in the list (ex. do not add "Plant" if there is "Tree" (Parent: "Plant") in the list)
    		// case 2: do not add labels that have the same parents as an item already in the list (ex. do not add "Pavement" (Parent: "Path") 
    		// if "Sidewalk" (Parent: "Path" is already in the list)
    		// !!! only exception: labels that have "Outdoors, Nature" as their parents (ex. add both "Ice" (Parents: Outdoors, Nature) and "Snow" (same)) 
    		// case 3: if the label's parents are "Clothing" or "Accessories" (separate function for these)
    		// !!! exception: if one of its parents is also "Person"
    		
    		// case "change an item":
    		// if a new item is a child of an item that is already in the list (ex. delete "Plant" if "Tree" (Parent: "Plant") is trying to get in)
    		
    		// special cases:
    		// #1:
    		
    		else {
    			if (label.getParents().size() == 2 
    					&& (label.getParents().get(0).getName().equals("Outdoors") && label.getParents().get(1).getName().equals("Nature"))) {
    				sceneryLabels.add(label);
    			}
    			else {
    				boolean person = false, clothingOrAccess = false; // "reject an item": case 3
    				for (Parent parent : label.getParents()) {
    					if((parent.getName().equals("Apparel") || parent.getName().equals("Clothing")) || 
    							(parent.getName().equals("Accessories") || parent.getName().equals("Accessory"))) {
    						clothingOrAccess = true;
    					} else if (parent.getName().equals("Person")) {
    						person = true;
    					}
    				}
    				
					if(clothingOrAccess && !person) {
						skip = true;
					}
					
	    			for (int i = 0; i < sceneryLabels.size(); i++) {
	    				if(skip) break;
	    				List<Parent> parents = sceneryLabels.get(i).getParents();
	    				for(Parent parent : parents) {
	    					if(skip) break;
	    					if(label.getName().equals(parent.getName())) { // "reject an item": case 1
	    						skip = true;
	    					}
	    				}
	    				if (parents.equals(label.getParents()) && !parents.isEmpty()) { // "reject an item": case 2
	    					skip = true;
	    				}
	    				List<Parent> labelParents = label.getParents(); // "change an item"
	    				for(Parent parent : labelParents) {
	    					if(sceneryLabels.get(i).getName().equals(parent.getName())) {
	    						sceneryLabels.set(i, label);
	    						skip = true;
	    					}
	    				}
	    			}
	        		
	    			if(!skip) {
	    				sceneryLabels.add(label);
	    			}
    			}
    		}
    	}
//    	List<Label> sceneryLabelsClean = new ArrayList<Label>();
//    	boolean remove = false;
//    	for(Label label : sceneryLabels) {
//    		remove = false;
//    		for (Label label2 : sceneryLabels) {
//    			List<Parent> parents = label2.getParents();
//    			for(Parent parent : parents) {
//    				if(label.getName().equals(parent.getName())) {
//    					remove = true;
//    				}
//    			}
//    		}
//    		if(remove) continue;
//			else sceneryLabelsClean.add(label);
//    	}
    	String sceneryLabelsString = "";
        if(!sceneryLabels.isEmpty()) { 
        	sceneryLabelsString = "This picture portrays:\n";
	    	for(Label label : sceneryLabels) {
	        	sceneryLabelsString += label.getName() + ",\n";
	        }
	    	sceneryLabelsString += "\n";
        }
    	return sceneryLabelsString;
    }
    
    public String describeClothing(List<Label> labels) {
    	List<Label> clothingLabels = new ArrayList<Label>();
    	// divide clothes into categories such as:
    	// (rule 1) Coat & Overcoat: if there is only Coat or Overcoat, add them; if there are their children (either "Coat" or "Overcoat") -> add only the first one
    	// (rule 2) Dress VS Skirt: whichever has the higher confidence
    	// (rule 3) Sleeve & Long Sleeve -> exclude both (non-descriptive)
    	boolean childOfCoat = false; // for rule 1
    	boolean skirtOrDress = false;
    	for(Label label : labels) {
    		// check whether this item is either clothing or accessories
    		// if not -> continue to the next label
    		List<Parent> parents = label.getParents();
    		boolean clothingOrAccess = false;
    		boolean skip = false;
    		for(Parent parent : parents) {
    			if(parent.getName().equals("Clothing") || parent.getName().equals("Accessories")) {
    				clothingOrAccess = true;
    				if(parent.getName().equals("Dress") || parent.getName().equals("Skirt")) {
    					if(!skirtOrDress) skirtOrDress = true;
    					else skip = true;
    				}
    			} 
    			if(parent.getName().equals("Person")) 
    				skip = true;
    		} 
    		if(!clothingOrAccess) continue;
    		
    		if(label.getName().equals("Sleeve") || label.getName().equals("Long Sleeve")) { // rule 3: exclude "Sleeve" & "Long Sleeve"
    			continue;
    		}
   
    		List<Parent> labelParents = label.getParents();
				for(Parent parent : labelParents) { // rule 2
	    			if((parent.getName().equals("Overcoat")) || (parent.getName().equals("Coat") && !label.getName().equals("Overcoat"))) { // rule 2
	    				if(!childOfCoat) {
	    					childOfCoat = true;
	    					break;
	    				}
	    				else {
	    					skip = true;
	    					break;
	    				}
	    			}
	    			if(parent.getName().equals("Dress") || parent.getName().equals("Skirt") || label.getName().equals("Dress") || label.getName().equals("Skirt")) {
	    				if(!skirtOrDress) skirtOrDress = true;
	    				else if(label.getName().equals("Skirt") || label.getName().equals("Dress")) skip = true;
	    			}
				}
    		
    		for (int i = 0; i < clothingLabels.size(); i++) {
    			if(skip) break;
    			parents = clothingLabels.get(i).getParents();
    			for(Parent parent : parents) {
    				if(skip) break;
    				if(label.getName().equals(parent.getName())) { // "reject an item": case 1
    					skip = true;
   					}
   				}
    			
   				if ((parents.equals(label.getParents()) && !parents.isEmpty()) &&
   						!(parents.get(0).getName().equals("Coat") && parents.get(1).getName().equals("Clothing"))) { // "reject an item": case 2
   					if((parents.size() != 1 && parents.get(0).getName().equals("Clothing")) || (parents.size() != 1 && parents.get(0).getName().equals("Accessories")))
   						skip = true;
   				}
   
   				// "change an item"
   				labelParents = label.getParents();
   				for(Parent parent : labelParents) {
   					if(clothingLabels.get(i).getName().equals(parent.getName())) {
   						//System.out.println("Change from " + clothingLabels.get(i).getName() + " to " + label.getName());
   						clothingLabels.set(i, label);
   						skip = true;
   					}
   				}
   				
   			}
        		
    		if(!skip) {
   				clothingLabels.add(label);
   			}
    	}
    	
    	if(childOfCoat) {
	    	Iterator<Label> iter = clothingLabels.iterator();
		    while (iter.hasNext()) {
		    	if(iter.next().getName().equals("Overcoat"))
		    		iter.remove();
		    }
    	}
    	
    	String clothingLabelsString = "\nThe " + gender + " is wearing and/or carrying the following items:\n"
    			+ "";
    	for(Label label : clothingLabels) {
    		clothingLabelsString += label.getName() + ",\n";
    	}
    	clothingLabelsString += "\n";
    	return clothingLabelsString;
    }
    
    public String describeFaces (List<FaceDetail> faceDetails) {
    	// more than 5 people -> just number of people + their gender
    	// less -> previous + all the other information
    	String descriptionFacesString = "";
    	
    	if(faceDetails.size() > 5) {
    		descriptionFacesString += faceDetails.size() + " people were detected. ";
    		int woman = 0, man = 0;
    		for(FaceDetail face : faceDetails) {
    			if(face.getGender().getValue().equals("Female")) woman++;
    			else man++;
    		}
    		descriptionFacesString += "There seem to be " + woman + " women and " + man + " men.";
    	} else if(faceDetails.size() < 5 && faceDetails.size() > 1) {
    		descriptionFacesString += faceDetails.size() + " faces were detected. Let's describe them one by one.\n";
        	for (int i = 0; i < faceDetails.size(); i++) {
                FaceDetail face = faceDetails.get(i);
                descriptionFacesString += (i + 1) + ") It seems to be ";
        		String pronoun = "";
        		if(face.getGender().getValue().equals("Female")) {
        			gender = "woman"; 
        			pronoun = "She";
        		}
        		else {
        			gender = "man";
        			pronoun = "He";
        		}
        		
        		float age = (face.getAgeRange().getHigh() + face.getAgeRange().getLow()) / 2;
        		if(age > 60) descriptionFacesString += "an old " + gender + ". \n";
        		else if(age > 40) descriptionFacesString += "a middle-aged " + gender + ". \n";
        		else if(age > 18) descriptionFacesString += "a young " + gender + ". \n";
        		else if(age <= 18) {
        			if(gender.equals("woman")) {
        				descriptionFacesString += "a girl. \n";
        				gender = "girl";
        			}
        			else {
        				descriptionFacesString += "a boy. \n";
        				gender = "boy";
        			}
        		}
        		
        		if(face.getSmile().getValue()) descriptionFacesString += pronoun + " seems to be smiling. \n";
        		if(face.getBeard().getValue()) descriptionFacesString += pronoun + " seems to have a beard. \n";
                if(face.getMustache().getValue()) descriptionFacesString += pronoun + " seems to have a mustache. \n";
                if(face.getSunglasses().getValue()) descriptionFacesString += pronoun + " seems to be wearing a pair of sunglasses. \n";
                if(face.getEyeglasses().getValue()) descriptionFacesString += pronoun + " seems to be wearing a pair of glasses. \n";
                
                List<Emotion> emotions = face.getEmotions();
                Emotion mainEmotion = new Emotion();
                float emotionConfidence = 0;
                for (Emotion emotion : emotions) {
             	   if(emotion.getConfidence() > emotionConfidence) {
             		   emotionConfidence = emotion.getConfidence();
             		   mainEmotion = emotion;
             	   }
                }
                descriptionFacesString += pronoun + " seems to be in a " + mainEmotion.getType().toLowerCase() + " mood. \n";
        	}
    	} else if(faceDetails.size() == 1) {
    		requiresClothingAnalysis = true;
    		descriptionFacesString += "In the picture, one face was detected. \nIt seems to be ";
    		FaceDetail face = faceDetails.get(0);
    		String pronoun = "";
    		if(face.getGender().getValue().equals("Female")) {
    			gender = "woman"; 
    			pronoun = "She";
    		}
    		else {
    			gender = "man";
    			pronoun = "He";
    		}
    		
    		float age = (face.getAgeRange().getHigh() + face.getAgeRange().getLow()) / 2;
    		if(age > 60) descriptionFacesString += "an old " + gender + ". \n";
    		else if(age > 40) descriptionFacesString += "a middle-aged " + gender + ". \n";
    		else if(age > 18) descriptionFacesString += "a young " + gender + ". \n";
    		else if(age <= 18) {
    			if(gender.equals("woman")) descriptionFacesString += "a girl. \n";
    			else descriptionFacesString += "a boy. \n";
    		}
    		
    		if(face.getSmile().getValue()) descriptionFacesString += pronoun + " seems to be smiling. \n";
    		if(face.getBeard().getValue()) descriptionFacesString += pronoun + " seems to have a beard. \n";
            if(face.getMustache().getValue()) descriptionFacesString += pronoun + " seems to have a mustache. \n";
            
            List<Emotion> emotions = face.getEmotions();
            Emotion mainEmotion = new Emotion();
            float emotionConfidence = 0;
            for (Emotion emotion : emotions) {
         	   if(emotion.getConfidence() > emotionConfidence) {
         		   emotionConfidence = emotion.getConfidence();
         		   mainEmotion = emotion;
         	   }
            }
            descriptionFacesString += "The " + gender + " seems to be in a " + mainEmotion.getType().toLowerCase() + " mood.\n";
   
    	} else return "";
    	return descriptionFacesString;
    }

}
