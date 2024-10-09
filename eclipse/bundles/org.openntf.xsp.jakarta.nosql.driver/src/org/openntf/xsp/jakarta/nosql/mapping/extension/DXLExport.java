/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakarta.nosql.mapping.extension;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.openntf.xsp.jakarta.nosql.communication.driver.DominoConstants;

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
