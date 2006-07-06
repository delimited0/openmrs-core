package org.openmrs.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PortletController implements Controller {

	protected Log log = LogFactory.getLog(this.getClass());

	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		// find the portlet that was identified in the openmrs:portlet taglib
		Object uri = request.getAttribute("javax.servlet.include.servlet_path");
		String portletPath = "";
		Map<String, Object> model = new HashMap<String, Object>();
		
		if (uri != null) {
			portletPath = uri.toString();

			// Allowable extensions are '' (no extension) and '.portlet'
			if (portletPath.endsWith("portlet"))
				portletPath = portletPath.replace(".portlet", "");
			else if (portletPath.endsWith("jsp"))
				throw new ServletException("Illegal extension used for portlet: '.jsp'. Allowable extensions are '' (no extension) and '.portlet'");

			log.debug("Loading portlet: " + portletPath);
			
			String size = (String)request.getAttribute("org.openmrs.portlet.size");
			Map<String, Object> params = (Map<String, Object>)request.getAttribute("org.openmrs.portlet.parameters");
			
			model.put("size", size);
			model.putAll(params);
			
			if (context != null) {
				model.put("authenticatedUser", context.getAuthenticatedUser());
				
				// if a patient id is available, put "obs" in the request
				Object o = request.getAttribute("patientId");
				if (o != null && !"".equals(o)) {
					Patient p = context.getPatientService().getPatient(Integer.valueOf((String)o));
					model.put("obs", context.getObsService().getObservations(p));
				}
				
				// if an encounter id is available, put its "obs" in the request
				o = request.getAttribute("encounterId");
				if (o != null && !"".equals(o)) {
					Encounter e = context.getEncounterService().getEncounter(Integer.valueOf((String)o));
					model.put("obs", context.getObsService().getObservations(e));
				}
				
			}
		}

		return new ModelAndView(portletPath, "model", model);

	}
}