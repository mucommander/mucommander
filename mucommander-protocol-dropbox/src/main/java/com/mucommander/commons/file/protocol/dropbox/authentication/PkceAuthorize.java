package com.mucommander.commons.file.protocol.dropbox.authentication;

import java.io.IOException;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxPKCEWebAuth;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;

public class PkceAuthorize {
	private DbxPKCEWebAuth pkceWebAuth;

	public PkceAuthorize(DbxAppInfo appInfo) {
		DbxRequestConfig requestConfig = new DbxRequestConfig("examples-authorize");
		DbxAppInfo appInfoWithoutSecret = new DbxAppInfo(appInfo.getKey());
		pkceWebAuth = new DbxPKCEWebAuth(requestConfig, appInfoWithoutSecret);
	}

	public String getAuthorizationUrl() {
		DbxWebAuth.Request webAuthRequest =  DbxWebAuth.newRequestBuilder()
				.withNoRedirect()
				.withTokenAccessType(TokenAccessType.OFFLINE)
				.build();

		return pkceWebAuth.authorize(webAuthRequest);
	}

	public DbxAuthFinish authorize(String code) throws IOException {
		try {
			// You must use the same DbxPKCEWebAuth to generate authorizationUrl and to handle code
			// exchange.
			return pkceWebAuth.finishFromCode(code);
		} catch (DbxException ex) {
			System.err.println("Error in DbxWebAuth.authorize: " + ex.getMessage());
			return null;
		}
	}
}
