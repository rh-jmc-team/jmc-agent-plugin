package org.openjdk.jmc.console.ext.agent.editor;

import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;

public class XmlScanner extends RuleBasedScanner {

	public XmlScanner(ColorManager manager) {
		IToken procInstr =
			new Token(
				new TextAttribute(
					manager.getColor(XmlColorConstants.PROC_INSTR)));

		IRule[] rules = new IRule[2];
		//Add rule for processing instructions
		rules[0] = new SingleLineRule("<?", "?>", procInstr);
		// Add generic whitespace rule.
		rules[1] = new WhitespaceRule(new XmlWhitespaceDetector());

		setRules(rules);
	}
}
