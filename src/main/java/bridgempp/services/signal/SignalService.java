package bridgempp.services.signal;

import java.util.List;
import java.util.Optional;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

import bridgempp.Message;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

public class SignalService extends BridgeService {

	private transient SignalServiceMessageSender signalServiceMessageSender;
	
	@Override
	public void connect() throws Exception {

	}

	@Override
	public void disconnect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendMessage(Message message) {
//		signalServiceMessageSender.sendMessage(new SignalServiceAddress(message.getDestination().getIdentifier()));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPersistent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setupSignal() {
//		IdentityKeyPair identityKey = KeyHelper.generateIdentityKeyPair();
//		List<PreKeyRecord> oneTimePreKeys = KeyHelper.generatePreKeys(0, 100);
//		SignedPreKeyRecord signedPreKeyRecord = KeyHelper.generateSignedPreKey(identityKey, signedPreKeyId);
//
//		SignalServiceAccountManager accountManager = new SignalServiceAccountManager(URL, TRUST_STORE, USERNAME,
//				PASSWORD);
//
//		accountManager.requestSmsVerificationCode();
//		accountManager.verifyAccount(receivedSmsVerificationCode, generateRandomSignalingKey(), false,
//				generateRandomInstallId());
//		accountManager.setGcmId(Optional.of(GoogleCloudMessaging.getInstance(this).register(REGISTRATION_ID)));
//		accountManager.setPreKeys(identityKey.getPublic(), lastResortKey, signedPreKey, oneTimePreKeys);
	}

}
