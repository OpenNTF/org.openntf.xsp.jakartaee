package org.openntf.xsp.nosql.mapping.extension;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.openntf.xsp.nosql.communication.driver.DominoConstants;

/**
 * This annotation provides a mechanism to customize the DXL export behavior
 * of a NoSQL entity field of name {@link DominoConstants#FIELD_DXL}.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface DXLExport {
	String attachmentOmittedText() default "";
	
	boolean convertNotesBitmapsToGIF() default false;
	
	String doctypeSYSTEM() default "";
	
	boolean exitOnFirstFatalError() default true;
	
	boolean forceNoteFormat() default false;
	
	boolean encapsulateMime() default false;
	
	String oleObjectOmittedText() default "";
	
	String[] omitItemNames() default {};
	
	boolean omitMiscFileObjects() default false;
	
	boolean omitOleObjects() default false;
	
	boolean omitRichTextAttachments() default false;
	
	boolean omitRichTextPictures() default false;
	
	boolean outputDOCTYPE() default true;
	
	String pictureOmittedText() default "";
	
	String[] restrictToItemNames() default {};
	
	boolean encapsulateRichText() default true;
}
