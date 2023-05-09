/*
 * Copyright 2022 ICCS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.ntua.ece.cslab.iwnet.bda.analyticsml.beans;

import java.util.Date;

public class MLModel {
	private int recipeId;
	private Date timestamp;
	private String osPath;
	
	public MLModel(int recipeId, Date timestamp, String ospath) {
		super();
		this.recipeId = recipeId;
		this.timestamp = timestamp;
		this.osPath = ospath;
	}

    public int getRecipeId() { return recipeId; }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public Date getTimestamp() { return timestamp; }

	public void setTimestamp(Date timestamp) {
	    this.timestamp = timestamp;
	}

    public String getOsPath() {
        return osPath;
    }

    public void setOsPath(String osPath) {
        this.osPath = osPath;
    }

}
