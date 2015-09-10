package com.mirasworks.server;

import com.mirasworks.server.http.WorksRequest;
import com.mirasworks.server.http.WorksResponse;
import com.mirasworks.server.http.exceptions.ExHttp;

public interface Imodule {

	public WorksResponse serve(WorksRequest request) throws ExHttp;
}
