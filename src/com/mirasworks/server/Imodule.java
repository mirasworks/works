package com.mirasworks.server;

import com.mirasworks.server.http.exceptions.Ex500;
import com.mirasworks.server.http.exceptions.ExNotMe;
import com.mirasworks.server.http.WorksRequest;
import com.mirasworks.server.http.WorksResponse;
import com.mirasworks.server.http.exceptions.Ex403Forbiden;

public interface Imodule {

	public WorksResponse serve(WorksRequest request) throws ExNotMe, Ex500, Ex403Forbiden ;
}
