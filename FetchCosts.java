package de.uni.freiburg.iig.telematik.swatiiplugin;

import org.w3c.dom.Element;

import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sepia.parser.pnml.PNMLParserException;

public class FetchCosts {
	 public void readPlaceCost(Element placeCostElement,String placename) throws PNMLParserException {
	        System.out.println(placename+ " "+ placeCostElement.getTextContent());

}
	 public void readTransitionCost(Element transitionCostElement,String transitionname) throws PNMLParserException {
		 System.out.println(transitionname+ " "+ transitionCostElement.getTextContent());

}
}