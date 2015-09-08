package com.mirasworks.module.files;

import com.mirasworks.server.Context;
import com.mirasworks.server.Imodule;
import com.mirasworks.server.http.WorksRequest;
import com.mirasworks.server.http.WorksResponse;
import com.mirasworks.server.http.exceptions.ExNotMe;

public class StaticFileModule  implements Imodule{

	public StaticFileModule(Context context) {
	}

	public WorksResponse serve(WorksRequest request) throws ExNotMe {
		throw new ExNotMe("nothing implemented yet");
	}

}
