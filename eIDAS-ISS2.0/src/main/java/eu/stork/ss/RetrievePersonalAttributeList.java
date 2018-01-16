package eu.stork.ss;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;

import eu.stork.peps.auth.commons.IPersonalAttributeList;

public abstract class RetrievePersonalAttributeList extends AbstractAction {
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = -7878347835293457787L;

	//Logger
	static final Logger logger = LoggerFactory.getLogger(RetrievePersonalAttributeList.class.getName());

	//The PAL
	private IPersonalAttributeList pal;

	/**
	 * Make sure this is a valid request from the service provider.
	 */
	public String execute() {
		HttpSession session;
		String message;
		String token = this.getServletRequest().getParameter(Constants.SP_PARAM_TOKEN);
		String sp=this.getServletRequest().getParameter("sp");//ptr
		this.getSession().setAttribute("sp", sp);
		String cc=this.getServletRequest().getParameter("cc");//ptr
		if (cc != null) this.getSession().setAttribute("cc", cc);
		String qaa=this.getServletRequest().getParameter("qaa");//ptr
		if (qaa != null) this.getSession().setAttribute("qaa", qaa);
		String saml=this.getServletRequest().getParameter("saml");//ptr
		if (saml != null) this.getSession().setAttribute("saml", saml);
		System.out.println("ssselectedId "+cc);

		if ( token==null ) {
			message = "The token is NULL. This indicates a bad formatted request.";

			logger.error(message);
			throw new ApplicationSpecificServiceException("The token is NULL!", message);
		}
		else {
			logger.debug("Token: " + token);
			//setPersonalAttributeList(retrievePersonalAttributeList(token));
			setPersonalAttributeList(retrievePersonalAttributeList(token,sp));//ptr

			if ( this.pal==null ) {
				message = "The PAL returned from retrievePersonalAttributeList is NULL. This either indicates an error with the SP or a connection problem.";

				logger.error(message);
				throw new ApplicationSpecificServiceException("The PAL is NULL!", message);
			}
			else {
				logger.trace("The PAL: " + pal.toString());

				session = this.getSession();

				session.setAttribute(Constants.SP_TOKEN, token);
				session.setAttribute(Constants.SP_PAL, this.pal);
				//session.setAttribute("COMM", sp);
			}
		}

		return Action.SUCCESS;
	}

	/**
	 * Retrieve and return the PAL.
	 * 
	 * @param token The token to use in order to retrieve the PAL.
	 * 
	 * @return The PAL or null if a problem occurs.
	 */
	//protected abstract IPersonalAttributeList retrievePersonalAttributeList(String token);
	public abstract IPersonalAttributeList retrievePersonalAttributeList(String token, String sp);//ptr

	/**
	 * Getter for the PAL.
	 * 
	 * @return The PAL.
	 */
	public IPersonalAttributeList getPersonalAttributeList() {
		return pal;
	}

	/**
	 * Setter for the PAL.
	 * 
	 * @param pal The PAL.
	 */
	public void setPersonalAttributeList(IPersonalAttributeList pal) {
		this.pal = pal;
	}
}