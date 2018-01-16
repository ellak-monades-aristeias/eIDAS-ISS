package eu.stork.ss;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.opensymphony.xwork2.ActionSupport;

/**
 * The AbstractSServiceAction class is an abstract action that will be used by
 * Supporting Service Actions.
 */
public abstract class AbstractAction extends ActionSupport implements
  ServletRequestAware, ServletResponseAware {
  
  /**
   * Unique identifier.
   */
  private static final long serialVersionUID = 7908546232602292326L;

  /**
   * Servlet servletResponse object, injected by struts.
   */
  private HttpServletResponse servletResponse;
  
  /**
   * Servlet servletRequest object, injected by struts.
   */
  private HttpServletRequest servletRequest;

  /**
   * {@inheritDoc}
   */
  public final HttpServletRequest getServletRequest() {
    return this.servletRequest;
  }
  
  /**
   * {@inheritDoc}
   */
  public final void setServletRequest(final HttpServletRequest nRequest) {
    this.servletRequest = nRequest;
  }
  
  /**
   * {@inheritDoc}
   */
  public final HttpServletResponse getServletResponse() {
    return this.servletResponse;
  }
  
  /**
   * {@inheritDoc}
   */
  public final void setServletResponse(final HttpServletResponse nResponse) {
    this.servletResponse = nResponse;
  }
  
  /**
   * Getter for the session object.
   * 
   * @return The session object.
   * 
   * @see HttpSession
   */
  public final HttpSession getSession() {
	return getServletRequest().getSession(true);
  }
}