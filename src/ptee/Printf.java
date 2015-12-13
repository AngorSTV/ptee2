package ptee;

import javax.swing.JTextArea;

public class Printf {

	//TODO доработать до i18n
	private JTextArea textArea;
	private CollectionOutput collectionOutput;
	
	public Printf() {
		
	}

	public void println (String str){
		switch (getOutput()){
		case CONSOLE:
			System.out.println(str);
			break;
		case TEXTAREA:
			textArea.append(str+"\n");
			textArea.setCaretPosition(textArea.getDocument().getLength());
			break;
		default:
			break;
		
		}
		
	}
	
	public void setOutput(CollectionOutput collectionOutput){
		this.collectionOutput = collectionOutput;
	}
	
	public void setEnv (JTextArea textArea){
		this.textArea = textArea;
	}

	public CollectionOutput getOutput() {
		return collectionOutput;
	}
}
