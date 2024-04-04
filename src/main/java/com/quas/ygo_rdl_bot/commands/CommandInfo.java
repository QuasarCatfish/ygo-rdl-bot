package com.quas.ygo_rdl_bot.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {

	public Class<?> parent() default void.class;
	public String name();
	public String desc();
	
	public boolean requiresPermissionView() default false;
	
	public DeferType deferSlash() default DeferType.Reply;
	public DeferType deferButton() default DeferType.Edit;
//	public DeferType deferSelect() default DeferType.Edit;
	public DeferType deferModal() default DeferType.Edit;
	
	public static enum DeferType {
		None, Reply, Edit;
	}
}
