package teammates.ui.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.FeedbackSessionAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.datatransfer.StudentResultBundle;
import teammates.common.datatransfer.SubmissionAttributes;
import teammates.common.util.Assumption;
import teammates.common.util.Constants;
import teammates.common.util.Sanitizer;
import teammates.common.util.StringHelper;
import teammates.common.util.Url;

/**
 * Data and utility methods needed to render a specific page.
 */
public class PageData {

	public static final String DISABLED = "style=\"color: gray; text-decoration: none;\" onclick=\"return false\"";

	/** The user for whom the pages are displayed (i.e. the 'nominal user'). 
	 *    May not be the logged in user (under masquerade mode) */
	public AccountAttributes account;

	/**
	 * @param account The account for the nominal user.
	 */
	public PageData(AccountAttributes account){
		this.account = account;
	}
	
	@SuppressWarnings("unused")
	private void _________general_util_methods(){
	//========================================================================	
	}
	
	/* These util methods simply delegate the work to the matching *Helper
	 * class. We keep them here so that JSP pages do not have to import
	 * those *Helper classes.
	 */
	public static String sanitizeForHtml(String unsanitizedStringLiteral){
		return Sanitizer.sanitizeForHtml(unsanitizedStringLiteral);
	}
	
	public static String sanitizeForJs(String unsanitizedStringLiteral){
		return Sanitizer.sanitizeForJs(unsanitizedStringLiteral);
	}
	
	public static String truncate (String untruncatedString, int truncateLength){
		return StringHelper.truncate(untruncatedString, truncateLength);
	}
	
	
	public String addUserIdToUrl(String link){
		return Url.addParamToUrl(link,Constants.PARAM_USER_ID,account.googleId);
	}
	
	/**
	 * Method to color the points by adding <code>span</code> tag with appropriate
	 * class (posDiff and negDiff).
	 * Positive points will be green, negative will be red, 0 will be black.
	 * This will also put N/A or Not Sure for respective points representation.
	 * The output will be E+x% for positive points, E-x% for negative points,
	 * and just E for equal share.
	 * Zero contribution will be printed as 0%
	 * @param points
	 * 		In terms of full percentage, so equal share will be 100, 20% more
	 * 		from equal share will be 120, etc.
	 */
	protected static String getPointsAsColorizedHtml(int points){
		if(points==Constants.POINTS_NOT_SUBMITTED || points==Constants.INT_UNINITIALIZED)
			return "<span class=\"color_negative\" onmouseover=\"ddrivetip('"+
				Constants.TOOLTIP_EVALUATION_SUBMISSION_NOT_AVAILABLE+"')\" onmouseout=\"hideddrivetip()\">N/A</span>";
		else if(points==Constants.POINTS_NOT_SURE)
			return "<span class=\"color_negative\" onmouseover=\"ddrivetip('"+
				Constants.TOOLTIP_EVALUATION_SUBMISSION_NOT_SURE+"')\" onmouseout=\"hideddrivetip()\">N/S</span>";
		else if(points==0)
			return "<span class=\"color_negative\">0%</span>";
		else if(points>100)
			return "<span class=\"color_positive\">E +"+(points-100)+"%</span>";
		else if(points<100)
			return "<span class=\"color_negative\">E -"+(100-points)+"%</span>";
		else
			return "<span class=\"color_neutral\">E</span>";
	}
	
	/**
	 * Method to color the points by adding <code>span</code> tag with appropriate
	 * class (posDiff and negDiff).
	 * Positive points will be green, negative will be red, 0 will be black.
	 * This will also put N/A or Not Sure for respective points representation.
	 * The output will be Equal Share + x% for positive points,
	 * Equal Share - x% for negative points,
	 * and just Equal Share for equal share.
	 * Zero contribution will be printed as 0%
	 * @see #colorizePoints
	 * @param points
	 * 		In terms of full percentage, so equal share will be 100, 20% more
	 * 		from equal share will be 120, etc.
	 * @param inline
	 * 		Whether or not the "Equal Share" and the percentage will be
	 * 		displayed in one line.
	 */
	protected static String getPointsInEqualShareFormatAsHtml(int points, boolean inline){
		int delta = 0;
		if (points == Constants.POINTS_NOT_SUBMITTED || points==Constants.INT_UNINITIALIZED) {
			return "<span class=\"color_negative\">N/A</span>";
		} else if (points == Constants.POINTS_NOT_SURE) {
			return "<span class=\"color_negative\">Not sure</span>";
		} else if (points == 0) {
			return "<span class=\"color_negative\">0%</span>";
		} else if (points > 100) {
			delta = points - 100;
			if(inline) return "Equal Share<span class=\"color_positive\"> + " + delta + "%</span>";
			else return "Equal Share<br /><span class=\"color_positive\"> + " + delta + "%</span>";
		} else if (points < 100) {
			delta = 100 - points;
			if(inline) return "Equal Share<span class=\"color_negative\"> - " + delta + "%</span>";
			else return "Equal Share<br /><span class=\"color_negative\"> - " + delta + "%</span>";
		} else {
			return "Equal Share";
		}
	}
	
	/**
	 * Prints the difference between Perceived and Claimed (normalized).
	 * Positive difference will be colored green, negative will be red, and
	 * neutral will be black.
	 * If any of the Perceived or Claimed is Not Available, the difference will
	 * be N/A, indicating not available.
	 * If any of the Perceived or Claimed is Not Sure, the difference will be
	 * N/S, indicating not sure.
	 * For each of the two special case above, a tooltip will be displayed to
	 * explain the meaning of the abbreviation.
	 */
	protected static String getPointsDiffAsHtml(StudentResultBundle sub){
		int claimed = sub.summary.claimedToInstructor;
		int perceived = sub.summary.perceivedToInstructor;
		int diff = perceived - claimed;
		if(perceived==Constants.POINTS_NOT_SUBMITTED || perceived==Constants.INT_UNINITIALIZED
				|| claimed==Constants.POINTS_NOT_SUBMITTED || claimed==Constants.INT_UNINITIALIZED){
			return "<span class=\"negDiff\" onmouseover=\"ddrivetip('"+Constants.TOOLTIP_EVALUATION_SUBMISSION_NOT_AVAILABLE+"')\" onmouseout=\"hideddrivetip()\">N/A</span>";
		} else if(perceived==Constants.POINTS_NOT_SURE || claimed==Constants.POINTS_NOT_SURE) {
			return "<span class=\"negDiff\" onmouseover=\"ddrivetip('"+Constants.TOOLTIP_EVALUATION_SUBMISSION_NOT_SURE+"')\" onmouseout=\"hideddrivetip()\">N/S</span>";
		} else if(diff>0){
			return "<span class=\"posDiff\">+"+diff+"%</span>";
		} else if(diff<0){
			return "<span class=\"negDiff\">"+diff+"%</span>";
		} else {
			return "<span>"+diff+"</span>";
		}
	}
	
	/**
	 * Returns the justification from the given submission data.
	 */
	protected static String getJustificationAsSanitizedHtml(SubmissionAttributes sub){
		if(sub.justification==null || sub.justification.getValue()==null
				|| sub.justification.getValue().equals(""))
			return "N/A";
		else return Sanitizer.sanitizeForHtml(sub.justification.getValue());
	}
	
	
	/**
	 * Formats P2P feedback.
	 * Make the headings bold, and converts newlines to html line breaks.
	 */
	protected static String getP2pFeedbackAsHtml(String str, boolean enabled){
		if(!enabled){
			return "<span style=\"font-style: italic;\">Disabled</span>";
		}
		if(str.equals("") || str == null){
			return "N/A";
		}
		return str.replace("&lt;&lt;What I appreciate about you as a team member&gt;&gt;:", "<span class=\"bold\">What I appreciate about you as a team member:</span>")
				.replace("&lt;&lt;Areas you can improve further&gt;&gt;:", "<span class=\"bold\">Areas you can improve further:</span>")
				.replace("&lt;&lt;Other comments&gt;&gt;:", "<span class=\"bold\">Other comments:</span>")
				.replace("\n", "<br>");
	}
	
	/**
	 * Returns the timezone options as HTML code.
	 * None is selected, since the selection should only be done in client side.
	 */
	protected ArrayList<String> getTimeZoneOptionsAsHtml(double existingTimeZone){
		double[] options = new double[]{-12,-11,-10,-9,-8,-7,-6,-5,-4.5,-4,-3.5,
										-3,-2,-1,0,1,2,3,3.5,4,4.5,5,5.5,5.75,6,
										7,8,9,10,11,12,13};
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i<options.length; i++){
			String temp = "UTC";
			if(options[i]!=0){
				if((int)options[i]==options[i])
					temp+=String.format(" %+03d:00", (int)options[i]);
				else
					temp+=String.format(" %+03d:%02d", (int)options[i],
							(int)(Math.abs(options[i]-(int)options[i])*300/5));
			}
			result.add("<option value=\""+formatAsString(options[i])+"\"" +
						(existingTimeZone==options[i]
							? "selected=\"selected\""
							: "") +
						">"+temp+"</option>");
		}
		return result;
	}
	
	/**
	 * Returns the grace period options as HTML code.
	 */
	protected ArrayList<String> getGracePeriodOptionsAsHtml(int existingGracePeriod){
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i<=30; i+=5){
			result.add("<option value=\""+i+"\"" +
						(isGracePeriodToBeSelected(existingGracePeriod, i) 
							? " selected=\"selected\"" : "") +
						">"+i+" mins</option>");
		}
		return result;
	}
	
	/**
	 * Returns the time options as HTML code.
	 * By default the selected one is the last one.
	 * @param selectCurrentTime
	 */
	public ArrayList<String> getTimeOptionsAsHtml(Date timeToShowAsSelected){
		ArrayList<String> result = new ArrayList<String>();
		for(int i=1; i<=24; i++){
			result.add("<option value=\""+i+"\"" +
						(isTimeToBeSelected(timeToShowAsSelected, i)
							? " selected=\"selected\""
							: "") +
						">" +
					   String.format("%04dH", i*100 - (i==24 ? 41 : 0)) +
					   "</option>");
		}
		return result;
	}
	
	
	@SuppressWarnings("unused")
	private void ___________methods_to_generate_student_links(){
	//========================================================================	
	}
	
	//TODO: methods below this point should be made 'protected' and only the
	//  child classes that need them should expose them using public methods
	//  with similar name. That way, we know which child needs which method.

	/**
	 * Returns the status of the student, whether he has joined the course.
	 * This is based on googleId, if it's null or empty, then we assume he
	 * has not joined the course yet.
	 * @return "Yet to Join" or "Joined"
	 */
	public String getStudentStatus(StudentAttributes student){
		if(student.googleId == null || student.googleId.equals("")){
			return Constants.STUDENT_COURSE_STATUS_YET_TO_JOIN;
		} else {
			return Constants.STUDENT_COURSE_STATUS_JOINED;
		}
	}

	/**
	 * @return The relative path to the student home page. 
	 * The user Id is encoded in the url as a parameter.
	 */
	public String getStudentHomeLink(){
		String link = Constants.ACTION_STUDENT_HOME;
		link = addUserIdToUrl(link);
		return link;
	}
	
	@SuppressWarnings("unused")
	private void ___________methods_to_generate_instructor_links(){
	//========================================================================	
	}
	
	/**
	 * @return The relative path to the instructor home page. 
	 * The user Id is encoded in the url as a parameter.
	 */
	public String getInstructorHomeLink(){
		String link = Constants.ACTION_INSTRUCTOR_HOME;
		link = addUserIdToUrl(link);
		return link;
	}
	
	public String getInstructorCourseLink(){
		String link = Constants.ACTION_INSTRUCTOR_COURSES;
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorCourseEnrollLink(String courseId){
		String link = Constants.ACTION_INSTRUCTOR_COURSE_ENROLL;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseId);
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorCourseEnrollSaveLink(String courseId){
		//TODO: instead of using this method, the form should include these data as hidden fields?
		String link = Constants.ACTION_INSTRUCTOR_COURSE_ENROLL_SAVE;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseId);
		link = addUserIdToUrl(link);
		return link;
	}

	public String getInstructorCourseDetailsLink(String courseID){
		String link = Constants.ACTION_INSTRUCTOR_COURSE_DETAILS;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseID); 
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorCourseEditLink(String courseID){
		String link = Constants.ACTION_INSTRUCTOR_COURSE_EDIT;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseID); 
		link = addUserIdToUrl(link);
		return link;
	}
	
	/**
	 * @param isHome True if the Browser should redirect to the Home page after the operation. 
	 */
	public String getInstructorCourseDeleteLink(String courseId, boolean isHome){
		String link = Constants.ACTION_INSTRUCTOR_COURSE_DELETE;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseId);
		link = Url.addParamToUrl(
				link,
				Constants.PARAM_NEXT_URL,(isHome? Constants.ACTION_INSTRUCTOR_HOME : Constants.ACTION_INSTRUCTOR_COURSES));
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorEvaluationLink(){
		String link = Constants.ACTION_INSTRUCTOR_EVALS;
		link = addUserIdToUrl(link);
		return link;
	}
	

	public String getInstructorEvaluationDeleteLink(String courseID, String evalName, String nextURL){
		String link = Constants.ACTION_INSTRUCTOR_EVAL_DELETE;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseID);
		link = Url.addParamToUrl(link,Constants.PARAM_EVALUATION_NAME,evalName);
		link = Url.addParamToUrl(link,Constants.PARAM_NEXT_URL,addUserIdToUrl(nextURL));
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorEvaluationEditLink(String courseID, String evalName){
		String link = Constants.ACTION_INSTRUCTOR_EVAL_EDIT;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseID);
		link = Url.addParamToUrl(link,Constants.PARAM_EVALUATION_NAME,evalName);
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorEvaluationResultsLink(String courseID, String evalName){
		String link = Constants.ACTION_INSTRUCTOR_EVAL_RESULTS;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseID);
		link = Url.addParamToUrl(link,Constants.PARAM_EVALUATION_NAME,evalName);
		link = addUserIdToUrl(link);
		return link;
	}

	
	public String getInstructorEvaluationRemindLink(String courseID, String evalName){
		String link = Constants.ACTION_INSTRUCTOR_EVAL_REMIND;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID, courseID);
		link = Url.addParamToUrl(link,Constants.PARAM_EVALUATION_NAME,evalName);
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorEvaluationPublishLink(String courseID, String evalName, boolean isHome){
		String link = Constants.ACTION_INSTRUCTOR_EVAL_PUBLISH;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID, courseID);
		link = Url.addParamToUrl(link,Constants.PARAM_EVALUATION_NAME,evalName);
		link = Url.addParamToUrl(link,Constants.PARAM_NEXT_URL,(isHome ? addUserIdToUrl(Constants.ACTION_INSTRUCTOR_HOME): addUserIdToUrl(Constants.ACTION_INSTRUCTOR_EVALS)));
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorEvaluationUnpublishLink(String courseID, String evalName, boolean isHome){
		String link = Constants.ACTION_INSTRUCTOR_EVAL_UNPUBLISH;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID, courseID);
		link = Url.addParamToUrl(link,Constants.PARAM_EVALUATION_NAME,evalName);
		link = Url.addParamToUrl(link,Constants.PARAM_NEXT_URL,(isHome ? addUserIdToUrl(Constants.ACTION_INSTRUCTOR_HOME): addUserIdToUrl(Constants.ACTION_INSTRUCTOR_EVALS)));
		link = addUserIdToUrl(link);
		return link;
	}
	

	public String getInstructorEvaluationSubmissionViewLink(String courseID, String evalName, String studentEmail){
		String link = Constants.ACTION_INSTRUCTOR_EVAL_SUBMISSION_VIEW;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseID);
		link = Url.addParamToUrl(link,Constants.PARAM_EVALUATION_NAME,evalName);
		link = Url.addParamToUrl(link,Constants.PARAM_STUDENT_EMAIL,studentEmail);
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorEvaluationSubmissionEditLink(String courseID, String evalName, String studentEmail){
		String link = Constants.ACTION_INSTRUCTOR_EVAL_SUBMISSION_EDIT;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseID);
		link = Url.addParamToUrl(link,Constants.PARAM_EVALUATION_NAME,evalName);
		link = Url.addParamToUrl(link,Constants.PARAM_STUDENT_EMAIL,studentEmail);
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorFeedbackSessionDeleteLink(String courseId, String feedbackSessionName, String nextURL){
		String link = Constants.ACTION_INSTRUCTOR_FEEDBACK_DELETE;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseId);
		link = Url.addParamToUrl(link,Constants.PARAM_FEEDBACK_SESSION_NAME,feedbackSessionName);
		link = Url.addParamToUrl(link,Constants.PARAM_NEXT_URL,addUserIdToUrl(nextURL));
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorFeedbackSessionEditLink(String courseId, String feedbackSessionName){
		String link = Constants.ACTION_INSTRUCTOR_FEEDBACK_EDIT;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseId);
		link = Url.addParamToUrl(link,Constants.PARAM_FEEDBACK_SESSION_NAME,feedbackSessionName);
		link = addUserIdToUrl(link);
		return link;
	}
	
	
	public String getInstructorFeedbackSessionResultsLink(String courseId, String feedbackSessionName){
		String link = Constants.ACTION_INSTRUCTOR_FEEDBACK_RESULTS;
		link = Url.addParamToUrl(link,Constants.PARAM_COURSE_ID,courseId);
		link = Url.addParamToUrl(link,Constants.PARAM_FEEDBACK_SESSION_NAME,feedbackSessionName);
		link = addUserIdToUrl(link);
		return link;
	}
		

	
	@SuppressWarnings("unused")
	private void _________other_util_methods_for_instructor_pages(){
	//========================================================================	
	}

	/**
	 * Returns the hover message to explain evaluation status
	 */
	public static String getInstructorHoverMessageForEval(EvaluationAttributes eval){
		switch(eval.getStatus()){
		case AWAITING: return Constants.TOOLTIP_EVALUATION_STATUS_AWAITING;
		case OPEN: return Constants.TOOLTIP_EVALUATION_STATUS_OPEN;
		case CLOSED: return Constants.TOOLTIP_EVALUATION_STATUS_CLOSED;
		case PUBLISHED: return Constants.TOOLTIP_EVALUATION_STATUS_PUBLISHED;
		default: 
			Assumption.fail("Unknown evaluation status :"+ eval.getStatus());
			return "unknown";
		}
	}

	/**
	 * Returns the links of actions available for a specific evaluation
	 * @param eval
	 * 		The Evaluation details
	 * @param position
	 * 		The position of the evaluation in the table (to be used for rowID)
	 * @param isHome
	 * 		Flag whether the link is to be put at homepage (to determine the redirect link in delete / publish)
	 * @return
	 */
	public String getInstructorEvaluationActions(EvaluationAttributes eval, int position, boolean isHome){
		StringBuffer result = new StringBuffer();
		
		boolean hasView = false;
		boolean hasEdit = false;
		boolean hasRemind = false;
		boolean hasPublish = false;
		boolean hasUnpublish = false;
		
		switch(eval.getStatus()){
		case AWAITING:
			hasEdit = true;
			break;
		case OPEN:
			hasView = true;
			hasEdit = true;
			hasRemind = true;
			break;
		case CLOSED:
			hasView = true;
			hasEdit = true;
			hasPublish = true;
			break;
		case PUBLISHED:
			hasUnpublish = true;
			hasView = true;
			break;
		default:
			Assumption.fail("Unknown evaluation status :"+ eval.getStatus());
			break;
			
		}
		
		result.append(
			"<a class=\"color_green t_eval_view"+ position + "\" " +
			"href=\"" + getInstructorEvaluationResultsLink(eval.courseId,eval.name) + "\" " +
			"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_EVALUATION_RESULTS+"')\" "+
			"onmouseout=\"hideddrivetip()\"" + (hasView ? "" : DISABLED) + ">View Results</a>"
		);
		result.append(
			"<a class=\"color_brown t_eval_edit" + position + "\" " +
			"href=\"" + getInstructorEvaluationEditLink(eval.courseId,eval.name) + "\" " +
			"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_EVALUATION_EDIT+"')\" onmouseout=\"hideddrivetip()\" " +
			(hasEdit ? "" : DISABLED) + ">Edit</a>"
		);
		result.append(
			"<a class=\"color_red t_eval_delete" + position + "\" " +
			"href=\"" + getInstructorEvaluationDeleteLink(eval.courseId,eval.name,(isHome ? Constants.ACTION_INSTRUCTOR_HOME : Constants.ACTION_INSTRUCTOR_EVALS)) + "\" " +
			"onclick=\"hideddrivetip(); return toggleDeleteEvaluationConfirmation('" + eval.courseId + "','" + eval.name + "');\" " +
			"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_EVALUATION_DELETE+"')\" onmouseout=\"hideddrivetip()\">Delete</a>"
		);
		result.append(
			"<a class=\"color_black t_eval_remind" + position + "\" " +
			"href=\"" + getInstructorEvaluationRemindLink(eval.courseId,eval.name) + "\" " +
			(hasRemind ? "onclick=\"hideddrivetip(); return toggleRemindStudents('" + eval.name + "');\" " : "") +
			"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_EVALUATION_REMIND+"')\" " +
			"onmouseout=\"hideddrivetip()\"" + (hasRemind ? "" : DISABLED) + ">Remind</a>"
		);
		
		if (hasUnpublish) {
			result.append(
				"<a class=\"color_black t_eval_unpublish" + position + "\" " +
				"href=\"" + getInstructorEvaluationUnpublishLink(eval.courseId,eval.name,isHome) + "\" " +
				"onclick=\"hideddrivetip(); return toggleUnpublishEvaluation('" + eval.name + "');\" " +
				"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_EVALUATION_UNPUBLISH+"')\" onmouseout=\"hideddrivetip()\">" +
				"Unpublish</a>"
			);
		} else {
			result.append(
				"<a class=\"color_black t_eval_publish" + position + "\" " +
				"href=\"" + getInstructorEvaluationPublishLink(eval.courseId,eval.name,isHome) + "\" " +
				(hasPublish ? "onclick=\"hideddrivetip(); return togglePublishEvaluation('" + eval.name + "');\" " : "") +
				"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_EVALUATION_PUBLISH+"')\" " +
				"onmouseout=\"hideddrivetip()\"" + (hasPublish ? "" : DISABLED) + ">Publish</a>"
			);
		}
		return result.toString();
	}

	public static String getInstructorStatusForFeedbackSession(FeedbackSessionAttributes session){
		if (session.isOpened()) {
			return "Open";
		} else if (session.isWaitingToOpen()) {
			if (session.isVisible()) {
				return "Visible";
			} else {
				return "Awaiting";
			}
		} else if (session.isPublished()) {
			return "Published";
		} else {
			return "Closed";
		}
	}

	public static String getInstructorHoverMessageForFeedbackSession(FeedbackSessionAttributes session){
		
		String msg = "The feedback session has been created";
		
		if (session.isVisible()) {
			msg += Constants.TOOLTIP_FEEDBACK_SESSION_STATUS_VISIBLE;
		}
		
		if (session.isOpened()) {
			msg += Constants.TOOLTIP_FEEDBACK_SESSION_STATUS_OPEN;
		} else if (session.isWaitingToOpen()) {
			msg += Constants.TOOLTIP_FEEDBACK_SESSION_STATUS_AWAITING;
		} else if(session.isClosed()) {
			msg += Constants.TOOLTIP_FEEDBACK_SESSION_STATUS_CLOSED;
		}
		
		if (session.isPublished()) {
			msg += Constants.TOOLTIP_FEEDBACK_SESSION_STATUS_PUBLISHED;
		}
		
		msg += ".";
		
		return msg;
	}

	/**
	 * Returns the links of actions available for a specific evaluation
	 * @param session
	 * 		The Evaluation details
	 * @param position
	 * 		The position of the evaluation in the table (to be used for rowID)
	 * @param isHome
	 * 		Flag whether the link is to be put at homepage (to determine the redirect link in delete / publish)
	 * @return
	 */
	public String getInstructorFeedbackSessionActions(FeedbackSessionAttributes session, int position, boolean isHome){
		StringBuffer result = new StringBuffer();
		
		// Allowing ALL instructors to view results regardless of publish state.
		// TODO: allow creator only?
		boolean hasView = true;
	
		result.append(
			"<a class=\"color_green t_fs_view"+ position + "\" " +
			"href=\"" + getInstructorFeedbackSessionResultsLink(session.courseId,session.feedbackSessionName) + "\" " +
			"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_FEEDBACK_SESSION_RESULTS+"')\" "+
			"onmouseout=\"hideddrivetip()\"" + (hasView ? "" : DISABLED) + ">View Results</a>"
		);
		result.append(
			"<a class=\"color_brown t_fs_edit" + position + "\" " +
			"href=\"" + getInstructorFeedbackSessionEditLink(session.courseId,session.feedbackSessionName) + "\" " +
			"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_FEEDBACK_SESSION_EDIT+"')\" onmouseout=\"hideddrivetip()\">Edit</a>"
		);
		result.append(
			"<a class=\"color_red t_fs_delete" + position + "\" " +
			"href=\"" + getInstructorFeedbackSessionDeleteLink(session.courseId,session.feedbackSessionName,(isHome ? Constants.ACTION_INSTRUCTOR_HOME : Constants.ACTION_INSTRUCTOR_FEEDBACKS)) + "\" " +
			"onclick=\"hideddrivetip(); return toggleDeleteFeedbackSessionConfirmation('" + session.courseId + "','" + session.feedbackSessionName + "');\" " +
			"onmouseover=\"ddrivetip('"+Constants.TOOLTIP_FEEDBACK_SESSION_DELETE+"')\" onmouseout=\"hideddrivetip()\">Delete</a>"
		);
		return result.toString();
	}

	/**
	 * Returns the evaluaton status. Can be any one of these:
	 * <ul>
	 * <li>AWAITING - When the evaluation start time is still in the future</li>
	 * <li>OPEN - When the evaluation is started and has not passed the deadline</li>
	 * <li>CLOSED - When the evaluation deadline has passed but not published yet</li>
	 * <li>PUBLISHED - When the evaluation results has been published to students</li>
	 * </ul>
	 */
	public static String getInstructorStatusForEval(EvaluationAttributes eval){
		switch(eval.getStatus()){
		case AWAITING: return Constants.INSTRUCTOR_EVALUATION_STATUS_AWAITING;
		case OPEN: return Constants.INSTRUCTOR_EVALUATION_STATUS_OPEN;
		case CLOSED: return Constants.INSTRUCTOR_EVALUATION_STATUS_CLOSED;
		default: return Constants.INSTRUCTOR_EVALUATION_STATUS_PUBLISHED;
		}
	}
	
	private boolean isTimeToBeSelected(Date timeToShowAsSelected, int hourOfTheOption){
		boolean isEditingExistingEvaluation = (timeToShowAsSelected!=null);
		if(isEditingExistingEvaluation){
			Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.setTime(timeToShowAsSelected);
			if(cal.get(Calendar.MINUTE)==0){
				if(cal.get(Calendar.HOUR_OF_DAY)==hourOfTheOption) return true;
			} else {
				if(hourOfTheOption==24) return true;
			}
		} else {
			if(hourOfTheOption==24) return true;
		}
		return false;
	}

	private boolean isGracePeriodToBeSelected(int existingGracePeriodValue, int gracePeriodOptionValue){
		int defaultGracePeriod = 15;
		boolean isEditingExistingEvaluation = (existingGracePeriodValue!=Constants.INT_UNINITIALIZED);
		if(isEditingExistingEvaluation){
			return gracePeriodOptionValue==existingGracePeriodValue;
		} else {
			return gracePeriodOptionValue==defaultGracePeriod;
		}
	}

	private static String formatAsString(double num){
		if((int)num==num) {
			return ""+(int)num;
		} else {
			return ""+num;
		}
	}
}