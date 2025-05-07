package com.quas.ygo_rdl_bot.data;

public enum CardStyle {
	ORIGINAL("Original"),
	ALT_ART("Alternate Art"),
	ALT_ART_2("2nd Alternate Art"),
	OR_STYLE("OR Style"),
	OR_STYLE_ALT_ART("OR Style Alternate Art");

	private String cardStyleName;
	private CardStyle(String cardStyleName) {
		this.cardStyleName = cardStyleName;
	}

	@Override
	public String toString() {
		return cardStyleName;
	}
}