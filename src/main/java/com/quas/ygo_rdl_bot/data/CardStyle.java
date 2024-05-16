package com.quas.ygo_rdl_bot.data;

public enum CardStyle {
	ORIGINAL("Original"), ALT_ART("Alternate Art"), OR_STYLE("OR Style");

	private String cardStyleName;
	private CardStyle(String cardStyleName) {
		this.cardStyleName = cardStyleName;
	}

	@Override
	public String toString() {
		return cardStyleName;
	}
}