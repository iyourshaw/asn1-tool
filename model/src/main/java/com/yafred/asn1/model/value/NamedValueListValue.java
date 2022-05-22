/*******************************************************************************
 * Copyright (C) 2022 Fred D7e (https://github.com/yafred)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.yafred.asn1.model.value;

import java.util.ArrayList;

import com.yafred.asn1.model.Value;

public class NamedValueListValue extends Value {
	ArrayList<NamedValue> valueList; 
	
	public NamedValueListValue(ArrayList<NamedValue> valueList) {
		this.valueList = valueList;
	}
	
    public ArrayList<NamedValue> getValueList() {
		return valueList;
	}

	public void setValueList(ArrayList<NamedValue> valueList) {
		this.valueList = valueList;
	}

	@Override
	public boolean isNamedValueList() {
        return true;
    }
}
