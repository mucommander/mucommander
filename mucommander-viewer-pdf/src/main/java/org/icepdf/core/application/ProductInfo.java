/*
 * Copyright 2006-2015 ICEsoft Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.application;

public class ProductInfo {

    /**
     * The company that owns this product.
     */
    public static String COMPANY = "ICEsoft Technologies, Inc.";

    /**
     * The name of the product.
     */
    public static String PRODUCT = "ICEpdf";

    /**
     * The 3 levels of version identification, e.g. 1.0.0.
     */
    public static String PRIMARY = "x";
    public static String SECONDARY = "x";
    public static String TERTIARY = "x";

    /**
     * The release type of the product (alpha, beta, production).
     */
    public static String RELEASE_TYPE = "x";

    /**
     * The build number.  Typically this would be tracked and maintained
     * by the build system (i.e. Ant).
     */
    public static String BUILD_NO = "x";

    /**
     * The revision number retrieved from the repository for this build.
     * This is substitued automatically by subversion.
     */
    public static String REVISION = "x";

    /**
     * Convenience method to get all the relevant product information.
     * @return
     */
    public String toString(){
        StringBuilder info = new StringBuilder();
        info.append( "\n" );
        info.append( COMPANY );
        info.append( "\n" );
        info.append( PRODUCT );
        info.append( " " );
        info.append( PRIMARY );
        info.append( "." );
        info.append( SECONDARY );
        info.append( "." );
        info.append( TERTIARY );
        info.append( " " );
        info.append( RELEASE_TYPE );
        info.append( "\n" );
        info.append( "Build number: " );
        info.append( BUILD_NO );
        info.append( "\n" );
        info.append( "Revision: " );
        info.append( REVISION );
        info.append( "\n" );
        return info.toString();
    }

    public String getVersion(){
        StringBuilder info = new StringBuilder();
        info.append( PRIMARY );
        info.append( "." );
        info.append( SECONDARY );
        info.append( "." );
        info.append( TERTIARY );
        info.append( " " );
        info.append( RELEASE_TYPE );
        return info.toString();
    }

	public static void main(String[] args) {
        ProductInfo app = new ProductInfo();
        System.out.println( app.toString() );
    }

}
