package io.mosip.registration.util.common;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.LoginMode;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dto.AuthNRequestDTO;
import io.mosip.registration.dto.AuthNSendOTPDTO;
import io.mosip.registration.dto.LoginUserDTO;
import io.mosip.registration.dto.OtpGeneratorRequestDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.BaseService;
import io.mosip.registration.util.healthcheck.RegistrationAppHealthCheckUtil;

/**
 * OTP Manager
 * 
 * @author Saravanan
 *
 */
@Component
public class OTPManager extends BaseService {

	/**
	 * Instance of LOGGER
	 */
	private static final Logger LOGGER = AppConfig.getLogger(OTPManager.class);

	public ResponseDTO getOTP(final String key) {

		LOGGER.info(LoggerConstants.OTP_MANAGER_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get OTP Started");

		// Create Response to return to UI layer
		ResponseDTO response = new ResponseDTO();

		/* Check Network Connectivity */
		if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {
			AuthNRequestDTO authNRequestDTO = new AuthNRequestDTO();
			AuthNSendOTPDTO authNSendOTPDTO = new AuthNSendOTPDTO();
			authNSendOTPDTO.setAppId(RegistrationConstants.REGISTRATION_CLIENT);
			authNSendOTPDTO.setLangCode(RegistrationConstants.ENGLISH_LANG_CODE);
			authNSendOTPDTO.setOtpChannel(Arrays
					.asList(ApplicationContext.map().get(RegistrationConstants.OTP_CHANNELS).toString().split(",")));
			authNSendOTPDTO.setUserId(key);
			authNSendOTPDTO.setUseridtype(RegistrationConstants.USER_ID_CODE);
			authNRequestDTO.setRequest(authNSendOTPDTO);
			OtpGeneratorRequestDTO otpGeneratorRequestDto = new OtpGeneratorRequestDTO();
			
			// prepare otpGeneratorRequestDto with specified key(EO Username) obtained 
			otpGeneratorRequestDto.setKey(key);

			try {
				// obtain otpGeneratorResponseDto from serviceDelegateUtil
				@SuppressWarnings("unchecked")
				HashMap<String, String> responseMap = (HashMap<String, String>) serviceDelegateUtil
						.post("send_otp", authNRequestDTO,RegistrationConstants.JOB_TRIGGER_POINT_USER);
				if (responseMap != null && responseMap.get("message") != null) {

					// create Success Response
					setSuccessResponse(response,
							RegistrationConstants.OTP_GENERATION_SUCCESS_MESSAGE + responseMap.get("message"), null);
				} else {
					// create Error Response
					setErrorResponse(response, RegistrationConstants.OTP_GENERATION_ERROR_MESSAGE, null);
				}

			} catch (RegBaseCheckedException | HttpClientErrorException | HttpServerErrorException
					| SocketTimeoutException | ResourceAccessException exception) {

				LOGGER.error(LoggerConstants.OTP_MANAGER_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
						RegistrationConstants.APPLICATION_ID, exception.getMessage() + ExceptionUtils.getStackTrace(exception));

				// create Error Response
				setErrorResponse(response, RegistrationConstants.OTP_GENERATION_ERROR_MESSAGE, null);

			} catch (IllegalStateException illegalStateException) {
				LOGGER.error(LoggerConstants.OTP_MANAGER_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
						RegistrationConstants.APPLICATION_ID, illegalStateException.getMessage() + ExceptionUtils.getStackTrace(illegalStateException));

				setErrorResponse(response, RegistrationConstants.CONNECTION_ERROR, null);

			}

		} else {
			setErrorResponse(response, RegistrationConstants.CONNECTION_ERROR, null);
		}

		LOGGER.info(LoggerConstants.OTP_MANAGER_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get OTP ended");

		return response;

	}

	public ResponseDTO validateOTP(String userId, String otp) {

		LOGGER.info(LoggerConstants.OTP_MANAGER_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Validate OTP Started");

		ResponseDTO responseDTO = new ResponseDTO();

		/* Check Network Connectivity */
		if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {

			if (ApplicationContext.map().get(RegistrationConstants.USER_DTO) == null) {
				ApplicationContext.map().put(RegistrationConstants.USER_DTO, new LoginUserDTO());
			}

			LoginUserDTO loginUserDTO = (LoginUserDTO) ApplicationContext.map().get(RegistrationConstants.USER_DTO);
			loginUserDTO.setUserId(userId);
			loginUserDTO.setOtp(otp);

			try {
				// Obtain otpValidatorResponseDto from service delegate util
				serviceDelegateUtil.getAuthToken(LoginMode.OTP);
				SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
				successResponseDTO.setCode("Validation Successful");
				responseDTO.setSuccessResponseDTO(successResponseDTO);
				setSuccessResponse(responseDTO, null, null);
			} catch (RegBaseCheckedException | HttpClientErrorException | HttpServerErrorException
					| ResourceAccessException | RegBaseUncheckedException exception) {

				LOGGER.error(LoggerConstants.OTP_MANAGER_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
						RegistrationConstants.APPLICATION_ID, exception.getMessage() + ExceptionUtils.getStackTrace(exception));

				setErrorResponse(responseDTO, RegistrationConstants.OTP_VALIDATION_ERROR_MESSAGE, null);

			}
		} else {
			setErrorResponse(responseDTO, RegistrationConstants.CONNECTION_ERROR, null);
		}

		LOGGER.info(LoggerConstants.OTP_MANAGER_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Validate OTP ended");

		return responseDTO;
	}
}
