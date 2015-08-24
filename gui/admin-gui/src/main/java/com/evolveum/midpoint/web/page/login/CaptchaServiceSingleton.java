package com.evolveum.midpoint.web.page.login;

import org.springframework.beans.factory.annotation.Autowired;

import com.octo.captcha.service.image.ImageCaptchaService;
import com.octo.captcha.service.multitype.GenericManageableCaptchaService;

public class CaptchaServiceSingleton {
	
	@Autowired
	private GenericManageableCaptchaService service;
	//@Autowired
	//private ImageCaptchaService instance ;
	//= new DefaultManageableImageCaptchaService();
	
	public GenericManageableCaptchaService getService() {
		return service;
	}
	
	public void setService(GenericManageableCaptchaService service) {
		this.service = service;
	}

	
	
}
