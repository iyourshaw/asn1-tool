package com.yafred.asn1.model;

public class DurationType extends RestrictedCharacterStringType {

    @Override
	public boolean isDurationType() {
        return true;
    }

    @Override
	public Tag getUniversalTag() {
        return new Tag(Integer.valueOf(34), TagClass.UNIVERSAL_TAG, null);
    }
    
	@Override
	public String getName() {
		return ("DURATION");
	}
}
