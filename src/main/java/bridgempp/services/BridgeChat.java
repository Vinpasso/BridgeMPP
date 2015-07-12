package bridgempp.services;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import bridgempp.BridgeService;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;

@Entity(name = "BRIDGE_CHAT_SERVICE")
@DiscriminatorValue(value = "BRIDGE_CHAT_SERVICE")
public class BridgeChat extends BridgeService {

	@Column(name = "HOST", nullable = false, length = 50)
	private String host;
	
	@Column(name = "PORT", nullable = false)
	private int port;
	
	private Socket socket;
	private Endpoint endpoint;
	private User user;
	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] {
			MessageFormat.HTML, MessageFormat.PLAIN_TEXT };

	@Override
	public void connect() {
		endpoint = DataManager.getOrNewEndpointForIdentifier("BridgeChat", this);
		user = DataManager.getOrNewUserForIdentifier("BridgeChatUser", this, endpoint);
		try {
			socket = new Socket(host, port);
			socket.getOutputStream().write(
					BridgeChatProtoBuf.BindingRequest.newBuilder()
							.setBindInfo("BridgeMPP").build().toByteArray());
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (true) {
							CommandInterpreter.processMessage(new Message(user, 
									endpoint, BridgeChatProtoBuf.UserEvent
											.parseFrom(socket.getInputStream())
											.getChatMessage(),
									getSupportedMessageFormats()[0]));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (NumberFormatException | IOException e) {
			ShadowManager.log(Level.SEVERE,
					null, e);
		}
	}

	@Override
	public void disconnect() {
		try {
			BridgeChatProtoBuf.UnbindRequest.newBuilder().build()
					.writeTo(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendMessage(Message message) {
		try {
			BridgeChatProtoBuf.UserEvent
					.newBuilder()
					.setUsername(message.getOrigin().toString())
					.setChatMessage(
							message.toComplexString(getSupportedMessageFormats()))
					.build().writeTo(socket.getOutputStream());
			;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "BridgeChat";
	}

	@Override
	public boolean isPersistent() {
		return false;
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}
	
	@Override
	public void interpretCommand(Message message) {
		message.getOrigin().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
	}
	

	public static final class BridgeChatProtoBuf {
		private BridgeChatProtoBuf() {
		}

		public static void registerAllExtensions(
				com.google.protobuf.ExtensionRegistry registry) {
		}

		public interface ModuleIntroOrBuilder extends
				com.google.protobuf.MessageOrBuilder {

			// required string long_name = 1;
			/**
			 * <code>required string long_name = 1;</code>
			 *
			 * <pre>
			 * for management
			 * </pre>
			 */
			boolean hasLongName();

			/**
			 * <code>required string long_name = 1;</code>
			 *
			 * <pre>
			 * for management
			 * </pre>
			 */
			java.lang.String getLongName();

			/**
			 * <code>required string long_name = 1;</code>
			 *
			 * <pre>
			 * for management
			 * </pre>
			 */
			com.google.protobuf.ByteString getLongNameBytes();

			// required string short_name = 2;
			/**
			 * <code>required string short_name = 2;</code>
			 *
			 * <pre>
			 * for name prefixing
			 * </pre>
			 */
			boolean hasShortName();

			/**
			 * <code>required string short_name = 2;</code>
			 *
			 * <pre>
			 * for name prefixing
			 * </pre>
			 */
			java.lang.String getShortName();

			/**
			 * <code>required string short_name = 2;</code>
			 *
			 * <pre>
			 * for name prefixing
			 * </pre>
			 */
			com.google.protobuf.ByteString getShortNameBytes();
		}

		/**
		 * Protobuf type {@code ModuleIntro}
		 */
		public static final class ModuleIntro extends
				com.google.protobuf.GeneratedMessage implements
				ModuleIntroOrBuilder {
			// Use ModuleIntro.newBuilder() to construct.
			private ModuleIntro(
					com.google.protobuf.GeneratedMessage.Builder<?> builder) {
				super(builder);
				this.unknownFields = builder.getUnknownFields();
			}

			private ModuleIntro(boolean noInit) {
				this.unknownFields = com.google.protobuf.UnknownFieldSet
						.getDefaultInstance();
			}

			private static final ModuleIntro defaultInstance;

			public static ModuleIntro getDefaultInstance() {
				return defaultInstance;
			}

			public ModuleIntro getDefaultInstanceForType() {
				return defaultInstance;
			}

			private final com.google.protobuf.UnknownFieldSet unknownFields;

			@java.lang.Override
			public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
				return this.unknownFields;
			}

			private ModuleIntro(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				initFields();
				com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
						.newBuilder();
				try {
					boolean done = false;
					while (!done) {
						int tag = input.readTag();
						switch (tag) {
						case 0:
							done = true;
							break;
						default: {
							if (!parseUnknownField(input, unknownFields,
									extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
						case 10: {
							bitField0_ |= 0x00000001;
							longName_ = input.readBytes();
							break;
						}
						case 18: {
							bitField0_ |= 0x00000002;
							shortName_ = input.readBytes();
							break;
						}
						}
					}
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					throw e.setUnfinishedMessage(this);
				} catch (java.io.IOException e) {
					throw new com.google.protobuf.InvalidProtocolBufferException(
							e.getMessage()).setUnfinishedMessage(this);
				} finally {
					this.unknownFields = unknownFields.build();
					makeExtensionsImmutable();
				}
			}

			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return BridgeChatProtoBuf.internal_static_ModuleIntro_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return BridgeChatProtoBuf.internal_static_ModuleIntro_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								BridgeChatProtoBuf.ModuleIntro.class,
								BridgeChatProtoBuf.ModuleIntro.Builder.class);
			}

			public static com.google.protobuf.Parser<ModuleIntro> PARSER = new com.google.protobuf.AbstractParser<ModuleIntro>() {
				public ModuleIntro parsePartialFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws com.google.protobuf.InvalidProtocolBufferException {
					return new ModuleIntro(input, extensionRegistry);
				}
			};

			@java.lang.Override
			public com.google.protobuf.Parser<ModuleIntro> getParserForType() {
				return PARSER;
			}

			private int bitField0_;
			// required string long_name = 1;
			public static final int LONG_NAME_FIELD_NUMBER = 1;
			private java.lang.Object longName_;

			/**
			 * <code>required string long_name = 1;</code>
			 *
			 * <pre>
			 * for management
			 * </pre>
			 */
			public boolean hasLongName() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			/**
			 * <code>required string long_name = 1;</code>
			 *
			 * <pre>
			 * for management
			 * </pre>
			 */
			public java.lang.String getLongName() {
				java.lang.Object ref = longName_;
				if (ref instanceof java.lang.String) {
					return (java.lang.String) ref;
				} else {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						longName_ = s;
					}
					return s;
				}
			}

			/**
			 * <code>required string long_name = 1;</code>
			 *
			 * <pre>
			 * for management
			 * </pre>
			 */
			public com.google.protobuf.ByteString getLongNameBytes() {
				java.lang.Object ref = longName_;
				if (ref instanceof java.lang.String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					longName_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			// required string short_name = 2;
			public static final int SHORT_NAME_FIELD_NUMBER = 2;
			private java.lang.Object shortName_;

			/**
			 * <code>required string short_name = 2;</code>
			 *
			 * <pre>
			 * for name prefixing
			 * </pre>
			 */
			public boolean hasShortName() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			/**
			 * <code>required string short_name = 2;</code>
			 *
			 * <pre>
			 * for name prefixing
			 * </pre>
			 */
			public java.lang.String getShortName() {
				java.lang.Object ref = shortName_;
				if (ref instanceof java.lang.String) {
					return (java.lang.String) ref;
				} else {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						shortName_ = s;
					}
					return s;
				}
			}

			/**
			 * <code>required string short_name = 2;</code>
			 *
			 * <pre>
			 * for name prefixing
			 * </pre>
			 */
			public com.google.protobuf.ByteString getShortNameBytes() {
				java.lang.Object ref = shortName_;
				if (ref instanceof java.lang.String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					shortName_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			private void initFields() {
				longName_ = "";
				shortName_ = "";
			}

			private byte memoizedIsInitialized = -1;

			public final boolean isInitialized() {
				byte isInitialized = memoizedIsInitialized;
				if (isInitialized != -1)
					return isInitialized == 1;

				if (!hasLongName()) {
					memoizedIsInitialized = 0;
					return false;
				}
				if (!hasShortName()) {
					memoizedIsInitialized = 0;
					return false;
				}
				memoizedIsInitialized = 1;
				return true;
			}

			public void writeTo(com.google.protobuf.CodedOutputStream output)
					throws java.io.IOException {
				getSerializedSize();
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					output.writeBytes(1, getLongNameBytes());
				}
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					output.writeBytes(2, getShortNameBytes());
				}
				getUnknownFields().writeTo(output);
			}

			private int memoizedSerializedSize = -1;

			public int getSerializedSize() {
				int size = memoizedSerializedSize;
				if (size != -1)
					return size;

				size = 0;
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBytesSize(1, getLongNameBytes());
				}
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBytesSize(2, getShortNameBytes());
				}
				size += getUnknownFields().getSerializedSize();
				memoizedSerializedSize = size;
				return size;
			}

			private static final long serialVersionUID = 0L;

			@java.lang.Override
			protected java.lang.Object writeReplace()
					throws java.io.ObjectStreamException {
				return super.writeReplace();
			}

			public static BridgeChatProtoBuf.ModuleIntro parseFrom(
					com.google.protobuf.ByteString data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseFrom(
					com.google.protobuf.ByteString data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseFrom(byte[] data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseFrom(byte[] data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseDelimitedFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseDelimitedFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseFrom(
					com.google.protobuf.CodedInputStream input)
					throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.ModuleIntro parseFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static Builder newBuilder() {
				return Builder.create();
			}

			public Builder newBuilderForType() {
				return newBuilder();
			}

			public static Builder newBuilder(
					BridgeChatProtoBuf.ModuleIntro prototype) {
				return newBuilder().mergeFrom(prototype);
			}

			public Builder toBuilder() {
				return newBuilder(this);
			}

			@java.lang.Override
			protected Builder newBuilderForType(
					com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				Builder builder = new Builder(parent);
				return builder;
			}

			/**
			 * Protobuf type {@code ModuleIntro}
			 */
			public static final class Builder extends
					com.google.protobuf.GeneratedMessage.Builder<Builder>
					implements BridgeChatProtoBuf.ModuleIntroOrBuilder {
				public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
					return BridgeChatProtoBuf.internal_static_ModuleIntro_descriptor;
				}

				protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
					return BridgeChatProtoBuf.internal_static_ModuleIntro_fieldAccessorTable
							.ensureFieldAccessorsInitialized(
									BridgeChatProtoBuf.ModuleIntro.class,
									BridgeChatProtoBuf.ModuleIntro.Builder.class);
				}

				// Construct using BridgeChat.ModuleIntro.newBuilder()
				private Builder() {
					maybeForceBuilderInitialization();
				}

				private Builder(
						com.google.protobuf.GeneratedMessage.BuilderParent parent) {
					super(parent);
					maybeForceBuilderInitialization();
				}

				private void maybeForceBuilderInitialization() {
					if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					}
				}

				private static Builder create() {
					return new Builder();
				}

				public Builder clear() {
					super.clear();
					longName_ = "";
					bitField0_ = (bitField0_ & ~0x00000001);
					shortName_ = "";
					bitField0_ = (bitField0_ & ~0x00000002);
					return this;
				}

				public Builder clone() {
					return create().mergeFrom(buildPartial());
				}

				public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
					return BridgeChatProtoBuf.internal_static_ModuleIntro_descriptor;
				}

				public BridgeChatProtoBuf.ModuleIntro getDefaultInstanceForType() {
					return BridgeChatProtoBuf.ModuleIntro.getDefaultInstance();
				}

				public BridgeChatProtoBuf.ModuleIntro build() {
					BridgeChatProtoBuf.ModuleIntro result = buildPartial();
					if (!result.isInitialized()) {
						throw newUninitializedMessageException(result);
					}
					return result;
				}

				public BridgeChatProtoBuf.ModuleIntro buildPartial() {
					BridgeChatProtoBuf.ModuleIntro result = new BridgeChatProtoBuf.ModuleIntro(
							this);
					int from_bitField0_ = bitField0_;
					int to_bitField0_ = 0;
					if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
						to_bitField0_ |= 0x00000001;
					}
					result.longName_ = longName_;
					if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
						to_bitField0_ |= 0x00000002;
					}
					result.shortName_ = shortName_;
					result.bitField0_ = to_bitField0_;
					onBuilt();
					return result;
				}

				public Builder mergeFrom(com.google.protobuf.Message other) {
					if (other instanceof BridgeChatProtoBuf.ModuleIntro) {
						return mergeFrom((BridgeChatProtoBuf.ModuleIntro) other);
					} else {
						super.mergeFrom(other);
						return this;
					}
				}

				public Builder mergeFrom(BridgeChatProtoBuf.ModuleIntro other) {
					if (other == BridgeChatProtoBuf.ModuleIntro
							.getDefaultInstance())
						return this;
					if (other.hasLongName()) {
						bitField0_ |= 0x00000001;
						longName_ = other.longName_;
						onChanged();
					}
					if (other.hasShortName()) {
						bitField0_ |= 0x00000002;
						shortName_ = other.shortName_;
						onChanged();
					}
					this.mergeUnknownFields(other.getUnknownFields());
					return this;
				}

				public final boolean isInitialized() {
					if (!hasLongName()) {

						return false;
					}
					if (!hasShortName()) {

						return false;
					}
					return true;
				}

				public Builder mergeFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws java.io.IOException {
					BridgeChatProtoBuf.ModuleIntro parsedMessage = null;
					try {
						parsedMessage = PARSER.parsePartialFrom(input,
								extensionRegistry);
					} catch (com.google.protobuf.InvalidProtocolBufferException e) {
						parsedMessage = (BridgeChatProtoBuf.ModuleIntro) e
								.getUnfinishedMessage();
						throw e;
					} finally {
						if (parsedMessage != null) {
							mergeFrom(parsedMessage);
						}
					}
					return this;
				}

				private int bitField0_;

				// required string long_name = 1;
				private java.lang.Object longName_ = "";

				/**
				 * <code>required string long_name = 1;</code>
				 *
				 * <pre>
				 * for management
				 * </pre>
				 */
				public boolean hasLongName() {
					return ((bitField0_ & 0x00000001) == 0x00000001);
				}

				/**
				 * <code>required string long_name = 1;</code>
				 *
				 * <pre>
				 * for management
				 * </pre>
				 */
				public java.lang.String getLongName() {
					java.lang.Object ref = longName_;
					if (!(ref instanceof java.lang.String)) {
						java.lang.String s = ((com.google.protobuf.ByteString) ref)
								.toStringUtf8();
						longName_ = s;
						return s;
					} else {
						return (java.lang.String) ref;
					}
				}

				/**
				 * <code>required string long_name = 1;</code>
				 *
				 * <pre>
				 * for management
				 * </pre>
				 */
				public com.google.protobuf.ByteString getLongNameBytes() {
					java.lang.Object ref = longName_;
					if (ref instanceof String) {
						com.google.protobuf.ByteString b = com.google.protobuf.ByteString
								.copyFromUtf8((java.lang.String) ref);
						longName_ = b;
						return b;
					} else {
						return (com.google.protobuf.ByteString) ref;
					}
				}

				/**
				 * <code>required string long_name = 1;</code>
				 *
				 * <pre>
				 * for management
				 * </pre>
				 */
				public Builder setLongName(java.lang.String value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000001;
					longName_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>required string long_name = 1;</code>
				 *
				 * <pre>
				 * for management
				 * </pre>
				 */
				public Builder clearLongName() {
					bitField0_ = (bitField0_ & ~0x00000001);
					longName_ = getDefaultInstance().getLongName();
					onChanged();
					return this;
				}

				/**
				 * <code>required string long_name = 1;</code>
				 *
				 * <pre>
				 * for management
				 * </pre>
				 */
				public Builder setLongNameBytes(
						com.google.protobuf.ByteString value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000001;
					longName_ = value;
					onChanged();
					return this;
				}

				// required string short_name = 2;
				private java.lang.Object shortName_ = "";

				/**
				 * <code>required string short_name = 2;</code>
				 *
				 * <pre>
				 * for name prefixing
				 * </pre>
				 */
				public boolean hasShortName() {
					return ((bitField0_ & 0x00000002) == 0x00000002);
				}

				/**
				 * <code>required string short_name = 2;</code>
				 *
				 * <pre>
				 * for name prefixing
				 * </pre>
				 */
				public java.lang.String getShortName() {
					java.lang.Object ref = shortName_;
					if (!(ref instanceof java.lang.String)) {
						java.lang.String s = ((com.google.protobuf.ByteString) ref)
								.toStringUtf8();
						shortName_ = s;
						return s;
					} else {
						return (java.lang.String) ref;
					}
				}

				/**
				 * <code>required string short_name = 2;</code>
				 *
				 * <pre>
				 * for name prefixing
				 * </pre>
				 */
				public com.google.protobuf.ByteString getShortNameBytes() {
					java.lang.Object ref = shortName_;
					if (ref instanceof String) {
						com.google.protobuf.ByteString b = com.google.protobuf.ByteString
								.copyFromUtf8((java.lang.String) ref);
						shortName_ = b;
						return b;
					} else {
						return (com.google.protobuf.ByteString) ref;
					}
				}

				/**
				 * <code>required string short_name = 2;</code>
				 *
				 * <pre>
				 * for name prefixing
				 * </pre>
				 */
				public Builder setShortName(java.lang.String value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000002;
					shortName_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>required string short_name = 2;</code>
				 *
				 * <pre>
				 * for name prefixing
				 * </pre>
				 */
				public Builder clearShortName() {
					bitField0_ = (bitField0_ & ~0x00000002);
					shortName_ = getDefaultInstance().getShortName();
					onChanged();
					return this;
				}

				/**
				 * <code>required string short_name = 2;</code>
				 *
				 * <pre>
				 * for name prefixing
				 * </pre>
				 */
				public Builder setShortNameBytes(
						com.google.protobuf.ByteString value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000002;
					shortName_ = value;
					onChanged();
					return this;
				}

				// @@protoc_insertion_point(builder_scope:ModuleIntro)
			}

			static {
				defaultInstance = new ModuleIntro(true);
				defaultInstance.initFields();
			}

			// @@protoc_insertion_point(class_scope:ModuleIntro)
		}

		public interface BindingRequestOrBuilder extends
				com.google.protobuf.MessageOrBuilder {

			// required string bind_info = 1;
			/**
			 * <code>required string bind_info = 1;</code>
			 */
			boolean hasBindInfo();

			/**
			 * <code>required string bind_info = 1;</code>
			 */
			java.lang.String getBindInfo();

			/**
			 * <code>required string bind_info = 1;</code>
			 */
			com.google.protobuf.ByteString getBindInfoBytes();
		}

		/**
		 * Protobuf type {@code BindingRequest}
		 */
		public static final class BindingRequest extends
				com.google.protobuf.GeneratedMessage implements
				BindingRequestOrBuilder {
			// Use BindingRequest.newBuilder() to construct.
			private BindingRequest(
					com.google.protobuf.GeneratedMessage.Builder<?> builder) {
				super(builder);
				this.unknownFields = builder.getUnknownFields();
			}

			private BindingRequest(boolean noInit) {
				this.unknownFields = com.google.protobuf.UnknownFieldSet
						.getDefaultInstance();
			}

			private static final BindingRequest defaultInstance;

			public static BindingRequest getDefaultInstance() {
				return defaultInstance;
			}

			public BindingRequest getDefaultInstanceForType() {
				return defaultInstance;
			}

			private final com.google.protobuf.UnknownFieldSet unknownFields;

			@java.lang.Override
			public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
				return this.unknownFields;
			}

			private BindingRequest(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				initFields();
				com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
						.newBuilder();
				try {
					boolean done = false;
					while (!done) {
						int tag = input.readTag();
						switch (tag) {
						case 0:
							done = true;
							break;
						default: {
							if (!parseUnknownField(input, unknownFields,
									extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
						case 10: {
							bitField0_ |= 0x00000001;
							bindInfo_ = input.readBytes();
							break;
						}
						}
					}
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					throw e.setUnfinishedMessage(this);
				} catch (java.io.IOException e) {
					throw new com.google.protobuf.InvalidProtocolBufferException(
							e.getMessage()).setUnfinishedMessage(this);
				} finally {
					this.unknownFields = unknownFields.build();
					makeExtensionsImmutable();
				}
			}

			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return BridgeChatProtoBuf.internal_static_BindingRequest_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return BridgeChatProtoBuf.internal_static_BindingRequest_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								BridgeChatProtoBuf.BindingRequest.class,
								BridgeChatProtoBuf.BindingRequest.Builder.class);
			}

			public static com.google.protobuf.Parser<BindingRequest> PARSER = new com.google.protobuf.AbstractParser<BindingRequest>() {
				public BindingRequest parsePartialFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws com.google.protobuf.InvalidProtocolBufferException {
					return new BindingRequest(input, extensionRegistry);
				}
			};

			@java.lang.Override
			public com.google.protobuf.Parser<BindingRequest> getParserForType() {
				return PARSER;
			}

			private int bitField0_;
			// required string bind_info = 1;
			public static final int BIND_INFO_FIELD_NUMBER = 1;
			private java.lang.Object bindInfo_;

			/**
			 * <code>required string bind_info = 1;</code>
			 */
			public boolean hasBindInfo() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			/**
			 * <code>required string bind_info = 1;</code>
			 */
			public java.lang.String getBindInfo() {
				java.lang.Object ref = bindInfo_;
				if (ref instanceof java.lang.String) {
					return (java.lang.String) ref;
				} else {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						bindInfo_ = s;
					}
					return s;
				}
			}

			/**
			 * <code>required string bind_info = 1;</code>
			 */
			public com.google.protobuf.ByteString getBindInfoBytes() {
				java.lang.Object ref = bindInfo_;
				if (ref instanceof java.lang.String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					bindInfo_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			private void initFields() {
				bindInfo_ = "";
			}

			private byte memoizedIsInitialized = -1;

			public final boolean isInitialized() {
				byte isInitialized = memoizedIsInitialized;
				if (isInitialized != -1)
					return isInitialized == 1;

				if (!hasBindInfo()) {
					memoizedIsInitialized = 0;
					return false;
				}
				memoizedIsInitialized = 1;
				return true;
			}

			public void writeTo(com.google.protobuf.CodedOutputStream output)
					throws java.io.IOException {
				getSerializedSize();
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					output.writeBytes(1, getBindInfoBytes());
				}
				getUnknownFields().writeTo(output);
			}

			private int memoizedSerializedSize = -1;

			public int getSerializedSize() {
				int size = memoizedSerializedSize;
				if (size != -1)
					return size;

				size = 0;
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBytesSize(1, getBindInfoBytes());
				}
				size += getUnknownFields().getSerializedSize();
				memoizedSerializedSize = size;
				return size;
			}

			private static final long serialVersionUID = 0L;

			@java.lang.Override
			protected java.lang.Object writeReplace()
					throws java.io.ObjectStreamException {
				return super.writeReplace();
			}

			public static BridgeChatProtoBuf.BindingRequest parseFrom(
					com.google.protobuf.ByteString data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.BindingRequest parseFrom(
					com.google.protobuf.ByteString data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.BindingRequest parseFrom(
					byte[] data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.BindingRequest parseFrom(
					byte[] data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.BindingRequest parseFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.BindingRequest parseFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.BindingRequest parseDelimitedFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input);
			}

			public static BridgeChatProtoBuf.BindingRequest parseDelimitedFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.BindingRequest parseFrom(
					com.google.protobuf.CodedInputStream input)
					throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.BindingRequest parseFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static Builder newBuilder() {
				return Builder.create();
			}

			public Builder newBuilderForType() {
				return newBuilder();
			}

			public static Builder newBuilder(
					BridgeChatProtoBuf.BindingRequest prototype) {
				return newBuilder().mergeFrom(prototype);
			}

			public Builder toBuilder() {
				return newBuilder(this);
			}

			@java.lang.Override
			protected Builder newBuilderForType(
					com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				Builder builder = new Builder(parent);
				return builder;
			}

			/**
			 * Protobuf type {@code BindingRequest}
			 */
			public static final class Builder extends
					com.google.protobuf.GeneratedMessage.Builder<Builder>
					implements BridgeChatProtoBuf.BindingRequestOrBuilder {
				public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
					return BridgeChatProtoBuf.internal_static_BindingRequest_descriptor;
				}

				protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
					return BridgeChatProtoBuf.internal_static_BindingRequest_fieldAccessorTable
							.ensureFieldAccessorsInitialized(
									BridgeChatProtoBuf.BindingRequest.class,
									BridgeChatProtoBuf.BindingRequest.Builder.class);
				}

				// Construct using BridgeChat.BindingRequest.newBuilder()
				private Builder() {
					maybeForceBuilderInitialization();
				}

				private Builder(
						com.google.protobuf.GeneratedMessage.BuilderParent parent) {
					super(parent);
					maybeForceBuilderInitialization();
				}

				private void maybeForceBuilderInitialization() {
					if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					}
				}

				private static Builder create() {
					return new Builder();
				}

				public Builder clear() {
					super.clear();
					bindInfo_ = "";
					bitField0_ = (bitField0_ & ~0x00000001);
					return this;
				}

				public Builder clone() {
					return create().mergeFrom(buildPartial());
				}

				public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
					return BridgeChatProtoBuf.internal_static_BindingRequest_descriptor;
				}

				public BridgeChatProtoBuf.BindingRequest getDefaultInstanceForType() {
					return BridgeChatProtoBuf.BindingRequest
							.getDefaultInstance();
				}

				public BridgeChatProtoBuf.BindingRequest build() {
					BridgeChatProtoBuf.BindingRequest result = buildPartial();
					if (!result.isInitialized()) {
						throw newUninitializedMessageException(result);
					}
					return result;
				}

				public BridgeChatProtoBuf.BindingRequest buildPartial() {
					BridgeChatProtoBuf.BindingRequest result = new BridgeChatProtoBuf.BindingRequest(
							this);
					int from_bitField0_ = bitField0_;
					int to_bitField0_ = 0;
					if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
						to_bitField0_ |= 0x00000001;
					}
					result.bindInfo_ = bindInfo_;
					result.bitField0_ = to_bitField0_;
					onBuilt();
					return result;
				}

				public Builder mergeFrom(com.google.protobuf.Message other) {
					if (other instanceof BridgeChatProtoBuf.BindingRequest) {
						return mergeFrom((BridgeChatProtoBuf.BindingRequest) other);
					} else {
						super.mergeFrom(other);
						return this;
					}
				}

				public Builder mergeFrom(BridgeChatProtoBuf.BindingRequest other) {
					if (other == BridgeChatProtoBuf.BindingRequest
							.getDefaultInstance())
						return this;
					if (other.hasBindInfo()) {
						bitField0_ |= 0x00000001;
						bindInfo_ = other.bindInfo_;
						onChanged();
					}
					this.mergeUnknownFields(other.getUnknownFields());
					return this;
				}

				public final boolean isInitialized() {
					if (!hasBindInfo()) {

						return false;
					}
					return true;
				}

				public Builder mergeFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws java.io.IOException {
					BridgeChatProtoBuf.BindingRequest parsedMessage = null;
					try {
						parsedMessage = PARSER.parsePartialFrom(input,
								extensionRegistry);
					} catch (com.google.protobuf.InvalidProtocolBufferException e) {
						parsedMessage = (BridgeChatProtoBuf.BindingRequest) e
								.getUnfinishedMessage();
						throw e;
					} finally {
						if (parsedMessage != null) {
							mergeFrom(parsedMessage);
						}
					}
					return this;
				}

				private int bitField0_;

				// required string bind_info = 1;
				private java.lang.Object bindInfo_ = "";

				/**
				 * <code>required string bind_info = 1;</code>
				 */
				public boolean hasBindInfo() {
					return ((bitField0_ & 0x00000001) == 0x00000001);
				}

				/**
				 * <code>required string bind_info = 1;</code>
				 */
				public java.lang.String getBindInfo() {
					java.lang.Object ref = bindInfo_;
					if (!(ref instanceof java.lang.String)) {
						java.lang.String s = ((com.google.protobuf.ByteString) ref)
								.toStringUtf8();
						bindInfo_ = s;
						return s;
					} else {
						return (java.lang.String) ref;
					}
				}

				/**
				 * <code>required string bind_info = 1;</code>
				 */
				public com.google.protobuf.ByteString getBindInfoBytes() {
					java.lang.Object ref = bindInfo_;
					if (ref instanceof String) {
						com.google.protobuf.ByteString b = com.google.protobuf.ByteString
								.copyFromUtf8((java.lang.String) ref);
						bindInfo_ = b;
						return b;
					} else {
						return (com.google.protobuf.ByteString) ref;
					}
				}

				/**
				 * <code>required string bind_info = 1;</code>
				 */
				public Builder setBindInfo(java.lang.String value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000001;
					bindInfo_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>required string bind_info = 1;</code>
				 */
				public Builder clearBindInfo() {
					bitField0_ = (bitField0_ & ~0x00000001);
					bindInfo_ = getDefaultInstance().getBindInfo();
					onChanged();
					return this;
				}

				/**
				 * <code>required string bind_info = 1;</code>
				 */
				public Builder setBindInfoBytes(
						com.google.protobuf.ByteString value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000001;
					bindInfo_ = value;
					onChanged();
					return this;
				}

				// @@protoc_insertion_point(builder_scope:BindingRequest)
			}

			static {
				defaultInstance = new BindingRequest(true);
				defaultInstance.initFields();
			}

			// @@protoc_insertion_point(class_scope:BindingRequest)
		}

		public interface BindingResponseOrBuilder extends
				com.google.protobuf.MessageOrBuilder {

			// required bool success = 1;
			/**
			 * <code>required bool success = 1;</code>
			 */
			boolean hasSuccess();

			/**
			 * <code>required bool success = 1;</code>
			 */
			boolean getSuccess();

			// optional string diagnostic = 2;
			/**
			 * <code>optional string diagnostic = 2;</code>
			 */
			boolean hasDiagnostic();

			/**
			 * <code>optional string diagnostic = 2;</code>
			 */
			java.lang.String getDiagnostic();

			/**
			 * <code>optional string diagnostic = 2;</code>
			 */
			com.google.protobuf.ByteString getDiagnosticBytes();
		}

		/**
		 * Protobuf type {@code BindingResponse}
		 */
		public static final class BindingResponse extends
				com.google.protobuf.GeneratedMessage implements
				BindingResponseOrBuilder {
			// Use BindingResponse.newBuilder() to construct.
			private BindingResponse(
					com.google.protobuf.GeneratedMessage.Builder<?> builder) {
				super(builder);
				this.unknownFields = builder.getUnknownFields();
			}

			private BindingResponse(boolean noInit) {
				this.unknownFields = com.google.protobuf.UnknownFieldSet
						.getDefaultInstance();
			}

			private static final BindingResponse defaultInstance;

			public static BindingResponse getDefaultInstance() {
				return defaultInstance;
			}

			public BindingResponse getDefaultInstanceForType() {
				return defaultInstance;
			}

			private final com.google.protobuf.UnknownFieldSet unknownFields;

			@java.lang.Override
			public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
				return this.unknownFields;
			}

			private BindingResponse(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				initFields();
				com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
						.newBuilder();
				try {
					boolean done = false;
					while (!done) {
						int tag = input.readTag();
						switch (tag) {
						case 0:
							done = true;
							break;
						default: {
							if (!parseUnknownField(input, unknownFields,
									extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
						case 8: {
							bitField0_ |= 0x00000001;
							success_ = input.readBool();
							break;
						}
						case 18: {
							bitField0_ |= 0x00000002;
							diagnostic_ = input.readBytes();
							break;
						}
						}
					}
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					throw e.setUnfinishedMessage(this);
				} catch (java.io.IOException e) {
					throw new com.google.protobuf.InvalidProtocolBufferException(
							e.getMessage()).setUnfinishedMessage(this);
				} finally {
					this.unknownFields = unknownFields.build();
					makeExtensionsImmutable();
				}
			}

			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return BridgeChatProtoBuf.internal_static_BindingResponse_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return BridgeChatProtoBuf.internal_static_BindingResponse_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								BridgeChatProtoBuf.BindingResponse.class,
								BridgeChatProtoBuf.BindingResponse.Builder.class);
			}

			public static com.google.protobuf.Parser<BindingResponse> PARSER = new com.google.protobuf.AbstractParser<BindingResponse>() {
				public BindingResponse parsePartialFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws com.google.protobuf.InvalidProtocolBufferException {
					return new BindingResponse(input, extensionRegistry);
				}
			};

			@java.lang.Override
			public com.google.protobuf.Parser<BindingResponse> getParserForType() {
				return PARSER;
			}

			private int bitField0_;
			// required bool success = 1;
			public static final int SUCCESS_FIELD_NUMBER = 1;
			private boolean success_;

			/**
			 * <code>required bool success = 1;</code>
			 */
			public boolean hasSuccess() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			/**
			 * <code>required bool success = 1;</code>
			 */
			public boolean getSuccess() {
				return success_;
			}

			// optional string diagnostic = 2;
			public static final int DIAGNOSTIC_FIELD_NUMBER = 2;
			private java.lang.Object diagnostic_;

			/**
			 * <code>optional string diagnostic = 2;</code>
			 */
			public boolean hasDiagnostic() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			/**
			 * <code>optional string diagnostic = 2;</code>
			 */
			public java.lang.String getDiagnostic() {
				java.lang.Object ref = diagnostic_;
				if (ref instanceof java.lang.String) {
					return (java.lang.String) ref;
				} else {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						diagnostic_ = s;
					}
					return s;
				}
			}

			/**
			 * <code>optional string diagnostic = 2;</code>
			 */
			public com.google.protobuf.ByteString getDiagnosticBytes() {
				java.lang.Object ref = diagnostic_;
				if (ref instanceof java.lang.String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					diagnostic_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			private void initFields() {
				success_ = false;
				diagnostic_ = "";
			}

			private byte memoizedIsInitialized = -1;

			public final boolean isInitialized() {
				byte isInitialized = memoizedIsInitialized;
				if (isInitialized != -1)
					return isInitialized == 1;

				if (!hasSuccess()) {
					memoizedIsInitialized = 0;
					return false;
				}
				memoizedIsInitialized = 1;
				return true;
			}

			public void writeTo(com.google.protobuf.CodedOutputStream output)
					throws java.io.IOException {
				getSerializedSize();
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					output.writeBool(1, success_);
				}
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					output.writeBytes(2, getDiagnosticBytes());
				}
				getUnknownFields().writeTo(output);
			}

			private int memoizedSerializedSize = -1;

			public int getSerializedSize() {
				int size = memoizedSerializedSize;
				if (size != -1)
					return size;

				size = 0;
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBoolSize(1, success_);
				}
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBytesSize(2, getDiagnosticBytes());
				}
				size += getUnknownFields().getSerializedSize();
				memoizedSerializedSize = size;
				return size;
			}

			private static final long serialVersionUID = 0L;

			@java.lang.Override
			protected java.lang.Object writeReplace()
					throws java.io.ObjectStreamException {
				return super.writeReplace();
			}

			public static BridgeChatProtoBuf.BindingResponse parseFrom(
					com.google.protobuf.ByteString data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.BindingResponse parseFrom(
					com.google.protobuf.ByteString data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.BindingResponse parseFrom(
					byte[] data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.BindingResponse parseFrom(
					byte[] data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.BindingResponse parseFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.BindingResponse parseFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.BindingResponse parseDelimitedFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input);
			}

			public static BridgeChatProtoBuf.BindingResponse parseDelimitedFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.BindingResponse parseFrom(
					com.google.protobuf.CodedInputStream input)
					throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.BindingResponse parseFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static Builder newBuilder() {
				return Builder.create();
			}

			public Builder newBuilderForType() {
				return newBuilder();
			}

			public static Builder newBuilder(
					BridgeChatProtoBuf.BindingResponse prototype) {
				return newBuilder().mergeFrom(prototype);
			}

			public Builder toBuilder() {
				return newBuilder(this);
			}

			@java.lang.Override
			protected Builder newBuilderForType(
					com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				Builder builder = new Builder(parent);
				return builder;
			}

			/**
			 * Protobuf type {@code BindingResponse}
			 */
			public static final class Builder extends
					com.google.protobuf.GeneratedMessage.Builder<Builder>
					implements BridgeChatProtoBuf.BindingResponseOrBuilder {
				public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
					return BridgeChatProtoBuf.internal_static_BindingResponse_descriptor;
				}

				protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
					return BridgeChatProtoBuf.internal_static_BindingResponse_fieldAccessorTable
							.ensureFieldAccessorsInitialized(
									BridgeChatProtoBuf.BindingResponse.class,
									BridgeChatProtoBuf.BindingResponse.Builder.class);
				}

				// Construct using BridgeChat.BindingResponse.newBuilder()
				private Builder() {
					maybeForceBuilderInitialization();
				}

				private Builder(
						com.google.protobuf.GeneratedMessage.BuilderParent parent) {
					super(parent);
					maybeForceBuilderInitialization();
				}

				private void maybeForceBuilderInitialization() {
					if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					}
				}

				private static Builder create() {
					return new Builder();
				}

				public Builder clear() {
					super.clear();
					success_ = false;
					bitField0_ = (bitField0_ & ~0x00000001);
					diagnostic_ = "";
					bitField0_ = (bitField0_ & ~0x00000002);
					return this;
				}

				public Builder clone() {
					return create().mergeFrom(buildPartial());
				}

				public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
					return BridgeChatProtoBuf.internal_static_BindingResponse_descriptor;
				}

				public BridgeChatProtoBuf.BindingResponse getDefaultInstanceForType() {
					return BridgeChatProtoBuf.BindingResponse
							.getDefaultInstance();
				}

				public BridgeChatProtoBuf.BindingResponse build() {
					BridgeChatProtoBuf.BindingResponse result = buildPartial();
					if (!result.isInitialized()) {
						throw newUninitializedMessageException(result);
					}
					return result;
				}

				public BridgeChatProtoBuf.BindingResponse buildPartial() {
					BridgeChatProtoBuf.BindingResponse result = new BridgeChatProtoBuf.BindingResponse(
							this);
					int from_bitField0_ = bitField0_;
					int to_bitField0_ = 0;
					if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
						to_bitField0_ |= 0x00000001;
					}
					result.success_ = success_;
					if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
						to_bitField0_ |= 0x00000002;
					}
					result.diagnostic_ = diagnostic_;
					result.bitField0_ = to_bitField0_;
					onBuilt();
					return result;
				}

				public Builder mergeFrom(com.google.protobuf.Message other) {
					if (other instanceof BridgeChatProtoBuf.BindingResponse) {
						return mergeFrom((BridgeChatProtoBuf.BindingResponse) other);
					} else {
						super.mergeFrom(other);
						return this;
					}
				}

				public Builder mergeFrom(
						BridgeChatProtoBuf.BindingResponse other) {
					if (other == BridgeChatProtoBuf.BindingResponse
							.getDefaultInstance())
						return this;
					if (other.hasSuccess()) {
						setSuccess(other.getSuccess());
					}
					if (other.hasDiagnostic()) {
						bitField0_ |= 0x00000002;
						diagnostic_ = other.diagnostic_;
						onChanged();
					}
					this.mergeUnknownFields(other.getUnknownFields());
					return this;
				}

				public final boolean isInitialized() {
					if (!hasSuccess()) {

						return false;
					}
					return true;
				}

				public Builder mergeFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws java.io.IOException {
					BridgeChatProtoBuf.BindingResponse parsedMessage = null;
					try {
						parsedMessage = PARSER.parsePartialFrom(input,
								extensionRegistry);
					} catch (com.google.protobuf.InvalidProtocolBufferException e) {
						parsedMessage = (BridgeChatProtoBuf.BindingResponse) e
								.getUnfinishedMessage();
						throw e;
					} finally {
						if (parsedMessage != null) {
							mergeFrom(parsedMessage);
						}
					}
					return this;
				}

				private int bitField0_;

				// required bool success = 1;
				private boolean success_;

				/**
				 * <code>required bool success = 1;</code>
				 */
				public boolean hasSuccess() {
					return ((bitField0_ & 0x00000001) == 0x00000001);
				}

				/**
				 * <code>required bool success = 1;</code>
				 */
				public boolean getSuccess() {
					return success_;
				}

				/**
				 * <code>required bool success = 1;</code>
				 */
				public Builder setSuccess(boolean value) {
					bitField0_ |= 0x00000001;
					success_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>required bool success = 1;</code>
				 */
				public Builder clearSuccess() {
					bitField0_ = (bitField0_ & ~0x00000001);
					success_ = false;
					onChanged();
					return this;
				}

				// optional string diagnostic = 2;
				private java.lang.Object diagnostic_ = "";

				/**
				 * <code>optional string diagnostic = 2;</code>
				 */
				public boolean hasDiagnostic() {
					return ((bitField0_ & 0x00000002) == 0x00000002);
				}

				/**
				 * <code>optional string diagnostic = 2;</code>
				 */
				public java.lang.String getDiagnostic() {
					java.lang.Object ref = diagnostic_;
					if (!(ref instanceof java.lang.String)) {
						java.lang.String s = ((com.google.protobuf.ByteString) ref)
								.toStringUtf8();
						diagnostic_ = s;
						return s;
					} else {
						return (java.lang.String) ref;
					}
				}

				/**
				 * <code>optional string diagnostic = 2;</code>
				 */
				public com.google.protobuf.ByteString getDiagnosticBytes() {
					java.lang.Object ref = diagnostic_;
					if (ref instanceof String) {
						com.google.protobuf.ByteString b = com.google.protobuf.ByteString
								.copyFromUtf8((java.lang.String) ref);
						diagnostic_ = b;
						return b;
					} else {
						return (com.google.protobuf.ByteString) ref;
					}
				}

				/**
				 * <code>optional string diagnostic = 2;</code>
				 */
				public Builder setDiagnostic(java.lang.String value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000002;
					diagnostic_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>optional string diagnostic = 2;</code>
				 */
				public Builder clearDiagnostic() {
					bitField0_ = (bitField0_ & ~0x00000002);
					diagnostic_ = getDefaultInstance().getDiagnostic();
					onChanged();
					return this;
				}

				/**
				 * <code>optional string diagnostic = 2;</code>
				 */
				public Builder setDiagnosticBytes(
						com.google.protobuf.ByteString value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000002;
					diagnostic_ = value;
					onChanged();
					return this;
				}

				// @@protoc_insertion_point(builder_scope:BindingResponse)
			}

			static {
				defaultInstance = new BindingResponse(true);
				defaultInstance.initFields();
			}

			// @@protoc_insertion_point(class_scope:BindingResponse)
		}

		public interface UnbindRequestOrBuilder extends
				com.google.protobuf.MessageOrBuilder {
		}

		/**
		 * Protobuf type {@code UnbindRequest}
		 */
		public static final class UnbindRequest extends
				com.google.protobuf.GeneratedMessage implements
				UnbindRequestOrBuilder {
			// Use UnbindRequest.newBuilder() to construct.
			private UnbindRequest(
					com.google.protobuf.GeneratedMessage.Builder<?> builder) {
				super(builder);
				this.unknownFields = builder.getUnknownFields();
			}

			private UnbindRequest(boolean noInit) {
				this.unknownFields = com.google.protobuf.UnknownFieldSet
						.getDefaultInstance();
			}

			private static final UnbindRequest defaultInstance;

			public static UnbindRequest getDefaultInstance() {
				return defaultInstance;
			}

			public UnbindRequest getDefaultInstanceForType() {
				return defaultInstance;
			}

			private final com.google.protobuf.UnknownFieldSet unknownFields;

			@java.lang.Override
			public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
				return this.unknownFields;
			}

			private UnbindRequest(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				initFields();
				com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
						.newBuilder();
				try {
					boolean done = false;
					while (!done) {
						int tag = input.readTag();
						switch (tag) {
						case 0:
							done = true;
							break;
						default: {
							if (!parseUnknownField(input, unknownFields,
									extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
						}
					}
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					throw e.setUnfinishedMessage(this);
				} catch (java.io.IOException e) {
					throw new com.google.protobuf.InvalidProtocolBufferException(
							e.getMessage()).setUnfinishedMessage(this);
				} finally {
					this.unknownFields = unknownFields.build();
					makeExtensionsImmutable();
				}
			}

			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return BridgeChatProtoBuf.internal_static_UnbindRequest_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return BridgeChatProtoBuf.internal_static_UnbindRequest_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								BridgeChatProtoBuf.UnbindRequest.class,
								BridgeChatProtoBuf.UnbindRequest.Builder.class);
			}

			public static com.google.protobuf.Parser<UnbindRequest> PARSER = new com.google.protobuf.AbstractParser<UnbindRequest>() {
				public UnbindRequest parsePartialFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws com.google.protobuf.InvalidProtocolBufferException {
					return new UnbindRequest(input, extensionRegistry);
				}
			};

			@java.lang.Override
			public com.google.protobuf.Parser<UnbindRequest> getParserForType() {
				return PARSER;
			}

			private void initFields() {
			}

			private byte memoizedIsInitialized = -1;

			public final boolean isInitialized() {
				byte isInitialized = memoizedIsInitialized;
				if (isInitialized != -1)
					return isInitialized == 1;

				memoizedIsInitialized = 1;
				return true;
			}

			public void writeTo(com.google.protobuf.CodedOutputStream output)
					throws java.io.IOException {
				getSerializedSize();
				getUnknownFields().writeTo(output);
			}

			private int memoizedSerializedSize = -1;

			public int getSerializedSize() {
				int size = memoizedSerializedSize;
				if (size != -1)
					return size;

				size = 0;
				size += getUnknownFields().getSerializedSize();
				memoizedSerializedSize = size;
				return size;
			}

			private static final long serialVersionUID = 0L;

			@java.lang.Override
			protected java.lang.Object writeReplace()
					throws java.io.ObjectStreamException {
				return super.writeReplace();
			}

			public static BridgeChatProtoBuf.UnbindRequest parseFrom(
					com.google.protobuf.ByteString data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseFrom(
					com.google.protobuf.ByteString data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseFrom(byte[] data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseFrom(
					byte[] data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseDelimitedFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseDelimitedFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseFrom(
					com.google.protobuf.CodedInputStream input)
					throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.UnbindRequest parseFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static Builder newBuilder() {
				return Builder.create();
			}

			public Builder newBuilderForType() {
				return newBuilder();
			}

			public static Builder newBuilder(
					BridgeChatProtoBuf.UnbindRequest prototype) {
				return newBuilder().mergeFrom(prototype);
			}

			public Builder toBuilder() {
				return newBuilder(this);
			}

			@java.lang.Override
			protected Builder newBuilderForType(
					com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				Builder builder = new Builder(parent);
				return builder;
			}

			/**
			 * Protobuf type {@code UnbindRequest}
			 */
			public static final class Builder extends
					com.google.protobuf.GeneratedMessage.Builder<Builder>
					implements BridgeChatProtoBuf.UnbindRequestOrBuilder {
				public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
					return BridgeChatProtoBuf.internal_static_UnbindRequest_descriptor;
				}

				protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
					return BridgeChatProtoBuf.internal_static_UnbindRequest_fieldAccessorTable
							.ensureFieldAccessorsInitialized(
									BridgeChatProtoBuf.UnbindRequest.class,
									BridgeChatProtoBuf.UnbindRequest.Builder.class);
				}

				// Construct using BridgeChat.UnbindRequest.newBuilder()
				private Builder() {
					maybeForceBuilderInitialization();
				}

				private Builder(
						com.google.protobuf.GeneratedMessage.BuilderParent parent) {
					super(parent);
					maybeForceBuilderInitialization();
				}

				private void maybeForceBuilderInitialization() {
					if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					}
				}

				private static Builder create() {
					return new Builder();
				}

				public Builder clear() {
					super.clear();
					return this;
				}

				public Builder clone() {
					return create().mergeFrom(buildPartial());
				}

				public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
					return BridgeChatProtoBuf.internal_static_UnbindRequest_descriptor;
				}

				public BridgeChatProtoBuf.UnbindRequest getDefaultInstanceForType() {
					return BridgeChatProtoBuf.UnbindRequest
							.getDefaultInstance();
				}

				public BridgeChatProtoBuf.UnbindRequest build() {
					BridgeChatProtoBuf.UnbindRequest result = buildPartial();
					if (!result.isInitialized()) {
						throw newUninitializedMessageException(result);
					}
					return result;
				}

				public BridgeChatProtoBuf.UnbindRequest buildPartial() {
					BridgeChatProtoBuf.UnbindRequest result = new BridgeChatProtoBuf.UnbindRequest(
							this);
					onBuilt();
					return result;
				}

				public Builder mergeFrom(com.google.protobuf.Message other) {
					if (other instanceof BridgeChatProtoBuf.UnbindRequest) {
						return mergeFrom((BridgeChatProtoBuf.UnbindRequest) other);
					} else {
						super.mergeFrom(other);
						return this;
					}
				}

				public Builder mergeFrom(BridgeChatProtoBuf.UnbindRequest other) {
					if (other == BridgeChatProtoBuf.UnbindRequest
							.getDefaultInstance())
						return this;
					this.mergeUnknownFields(other.getUnknownFields());
					return this;
				}

				public final boolean isInitialized() {
					return true;
				}

				public Builder mergeFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws java.io.IOException {
					BridgeChatProtoBuf.UnbindRequest parsedMessage = null;
					try {
						parsedMessage = PARSER.parsePartialFrom(input,
								extensionRegistry);
					} catch (com.google.protobuf.InvalidProtocolBufferException e) {
						parsedMessage = (BridgeChatProtoBuf.UnbindRequest) e
								.getUnfinishedMessage();
						throw e;
					} finally {
						if (parsedMessage != null) {
							mergeFrom(parsedMessage);
						}
					}
					return this;
				}

				// @@protoc_insertion_point(builder_scope:UnbindRequest)
			}

			static {
				defaultInstance = new UnbindRequest(true);
				defaultInstance.initFields();
			}

			// @@protoc_insertion_point(class_scope:UnbindRequest)
		}

		public interface UserStatusOrBuilder extends
				com.google.protobuf.MessageOrBuilder {

			// optional bool online_status = 2;
			/**
			 * <code>optional bool online_status = 2;</code>
			 */
			boolean hasOnlineStatus();

			/**
			 * <code>optional bool online_status = 2;</code>
			 */
			boolean getOnlineStatus();
		}

		/**
		 * Protobuf type {@code UserStatus}
		 */
		public static final class UserStatus extends
				com.google.protobuf.GeneratedMessage implements
				UserStatusOrBuilder {
			// Use UserStatus.newBuilder() to construct.
			private UserStatus(
					com.google.protobuf.GeneratedMessage.Builder<?> builder) {
				super(builder);
				this.unknownFields = builder.getUnknownFields();
			}

			private UserStatus(boolean noInit) {
				this.unknownFields = com.google.protobuf.UnknownFieldSet
						.getDefaultInstance();
			}

			private static final UserStatus defaultInstance;

			public static UserStatus getDefaultInstance() {
				return defaultInstance;
			}

			public UserStatus getDefaultInstanceForType() {
				return defaultInstance;
			}

			private final com.google.protobuf.UnknownFieldSet unknownFields;

			@java.lang.Override
			public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
				return this.unknownFields;
			}

			private UserStatus(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				initFields();
				com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
						.newBuilder();
				try {
					boolean done = false;
					while (!done) {
						int tag = input.readTag();
						switch (tag) {
						case 0:
							done = true;
							break;
						default: {
							if (!parseUnknownField(input, unknownFields,
									extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
						case 16: {
							bitField0_ |= 0x00000001;
							onlineStatus_ = input.readBool();
							break;
						}
						}
					}
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					throw e.setUnfinishedMessage(this);
				} catch (java.io.IOException e) {
					throw new com.google.protobuf.InvalidProtocolBufferException(
							e.getMessage()).setUnfinishedMessage(this);
				} finally {
					this.unknownFields = unknownFields.build();
					makeExtensionsImmutable();
				}
			}

			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return BridgeChatProtoBuf.internal_static_UserStatus_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return BridgeChatProtoBuf.internal_static_UserStatus_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								BridgeChatProtoBuf.UserStatus.class,
								BridgeChatProtoBuf.UserStatus.Builder.class);
			}

			public static com.google.protobuf.Parser<UserStatus> PARSER = new com.google.protobuf.AbstractParser<UserStatus>() {
				public UserStatus parsePartialFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws com.google.protobuf.InvalidProtocolBufferException {
					return new UserStatus(input, extensionRegistry);
				}
			};

			@java.lang.Override
			public com.google.protobuf.Parser<UserStatus> getParserForType() {
				return PARSER;
			}

			private int bitField0_;
			// optional bool online_status = 2;
			public static final int ONLINE_STATUS_FIELD_NUMBER = 2;
			private boolean onlineStatus_;

			/**
			 * <code>optional bool online_status = 2;</code>
			 */
			public boolean hasOnlineStatus() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			/**
			 * <code>optional bool online_status = 2;</code>
			 */
			public boolean getOnlineStatus() {
				return onlineStatus_;
			}

			private void initFields() {
				onlineStatus_ = false;
			}

			private byte memoizedIsInitialized = -1;

			public final boolean isInitialized() {
				byte isInitialized = memoizedIsInitialized;
				if (isInitialized != -1)
					return isInitialized == 1;

				memoizedIsInitialized = 1;
				return true;
			}

			public void writeTo(com.google.protobuf.CodedOutputStream output)
					throws java.io.IOException {
				getSerializedSize();
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					output.writeBool(2, onlineStatus_);
				}
				getUnknownFields().writeTo(output);
			}

			private int memoizedSerializedSize = -1;

			public int getSerializedSize() {
				int size = memoizedSerializedSize;
				if (size != -1)
					return size;

				size = 0;
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBoolSize(2, onlineStatus_);
				}
				size += getUnknownFields().getSerializedSize();
				memoizedSerializedSize = size;
				return size;
			}

			private static final long serialVersionUID = 0L;

			@java.lang.Override
			protected java.lang.Object writeReplace()
					throws java.io.ObjectStreamException {
				return super.writeReplace();
			}

			public static BridgeChatProtoBuf.UserStatus parseFrom(
					com.google.protobuf.ByteString data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.UserStatus parseFrom(
					com.google.protobuf.ByteString data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UserStatus parseFrom(byte[] data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.UserStatus parseFrom(byte[] data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UserStatus parseFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.UserStatus parseFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UserStatus parseDelimitedFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input);
			}

			public static BridgeChatProtoBuf.UserStatus parseDelimitedFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UserStatus parseFrom(
					com.google.protobuf.CodedInputStream input)
					throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.UserStatus parseFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static Builder newBuilder() {
				return Builder.create();
			}

			public Builder newBuilderForType() {
				return newBuilder();
			}

			public static Builder newBuilder(
					BridgeChatProtoBuf.UserStatus prototype) {
				return newBuilder().mergeFrom(prototype);
			}

			public Builder toBuilder() {
				return newBuilder(this);
			}

			@java.lang.Override
			protected Builder newBuilderForType(
					com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				Builder builder = new Builder(parent);
				return builder;
			}

			/**
			 * Protobuf type {@code UserStatus}
			 */
			public static final class Builder extends
					com.google.protobuf.GeneratedMessage.Builder<Builder>
					implements BridgeChatProtoBuf.UserStatusOrBuilder {
				public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
					return BridgeChatProtoBuf.internal_static_UserStatus_descriptor;
				}

				protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
					return BridgeChatProtoBuf.internal_static_UserStatus_fieldAccessorTable
							.ensureFieldAccessorsInitialized(
									BridgeChatProtoBuf.UserStatus.class,
									BridgeChatProtoBuf.UserStatus.Builder.class);
				}

				// Construct using BridgeChat.UserStatus.newBuilder()
				private Builder() {
					maybeForceBuilderInitialization();
				}

				private Builder(
						com.google.protobuf.GeneratedMessage.BuilderParent parent) {
					super(parent);
					maybeForceBuilderInitialization();
				}

				private void maybeForceBuilderInitialization() {
					if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					}
				}

				private static Builder create() {
					return new Builder();
				}

				public Builder clear() {
					super.clear();
					onlineStatus_ = false;
					bitField0_ = (bitField0_ & ~0x00000001);
					return this;
				}

				public Builder clone() {
					return create().mergeFrom(buildPartial());
				}

				public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
					return BridgeChatProtoBuf.internal_static_UserStatus_descriptor;
				}

				public BridgeChatProtoBuf.UserStatus getDefaultInstanceForType() {
					return BridgeChatProtoBuf.UserStatus.getDefaultInstance();
				}

				public BridgeChatProtoBuf.UserStatus build() {
					BridgeChatProtoBuf.UserStatus result = buildPartial();
					if (!result.isInitialized()) {
						throw newUninitializedMessageException(result);
					}
					return result;
				}

				public BridgeChatProtoBuf.UserStatus buildPartial() {
					BridgeChatProtoBuf.UserStatus result = new BridgeChatProtoBuf.UserStatus(
							this);
					int from_bitField0_ = bitField0_;
					int to_bitField0_ = 0;
					if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
						to_bitField0_ |= 0x00000001;
					}
					result.onlineStatus_ = onlineStatus_;
					result.bitField0_ = to_bitField0_;
					onBuilt();
					return result;
				}

				public Builder mergeFrom(com.google.protobuf.Message other) {
					if (other instanceof BridgeChatProtoBuf.UserStatus) {
						return mergeFrom((BridgeChatProtoBuf.UserStatus) other);
					} else {
						super.mergeFrom(other);
						return this;
					}
				}

				public Builder mergeFrom(BridgeChatProtoBuf.UserStatus other) {
					if (other == BridgeChatProtoBuf.UserStatus
							.getDefaultInstance())
						return this;
					if (other.hasOnlineStatus()) {
						setOnlineStatus(other.getOnlineStatus());
					}
					this.mergeUnknownFields(other.getUnknownFields());
					return this;
				}

				public final boolean isInitialized() {
					return true;
				}

				public Builder mergeFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws java.io.IOException {
					BridgeChatProtoBuf.UserStatus parsedMessage = null;
					try {
						parsedMessage = PARSER.parsePartialFrom(input,
								extensionRegistry);
					} catch (com.google.protobuf.InvalidProtocolBufferException e) {
						parsedMessage = (BridgeChatProtoBuf.UserStatus) e
								.getUnfinishedMessage();
						throw e;
					} finally {
						if (parsedMessage != null) {
							mergeFrom(parsedMessage);
						}
					}
					return this;
				}

				private int bitField0_;

				// optional bool online_status = 2;
				private boolean onlineStatus_;

				/**
				 * <code>optional bool online_status = 2;</code>
				 */
				public boolean hasOnlineStatus() {
					return ((bitField0_ & 0x00000001) == 0x00000001);
				}

				/**
				 * <code>optional bool online_status = 2;</code>
				 */
				public boolean getOnlineStatus() {
					return onlineStatus_;
				}

				/**
				 * <code>optional bool online_status = 2;</code>
				 */
				public Builder setOnlineStatus(boolean value) {
					bitField0_ |= 0x00000001;
					onlineStatus_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>optional bool online_status = 2;</code>
				 */
				public Builder clearOnlineStatus() {
					bitField0_ = (bitField0_ & ~0x00000001);
					onlineStatus_ = false;
					onChanged();
					return this;
				}

				// @@protoc_insertion_point(builder_scope:UserStatus)
			}

			static {
				defaultInstance = new UserStatus(true);
				defaultInstance.initFields();
			}

			// @@protoc_insertion_point(class_scope:UserStatus)
		}

		public interface UserEventOrBuilder extends
				com.google.protobuf.MessageOrBuilder {

			// required string username = 1;
			/**
			 * <code>required string username = 1;</code>
			 */
			boolean hasUsername();

			/**
			 * <code>required string username = 1;</code>
			 */
			java.lang.String getUsername();

			/**
			 * <code>required string username = 1;</code>
			 */
			com.google.protobuf.ByteString getUsernameBytes();

			// optional string plugin_id = 2;
			/**
			 * <code>optional string plugin_id = 2;</code>
			 *
			 * <pre>
			 * for foreign users
			 * </pre>
			 */
			boolean hasPluginId();

			/**
			 * <code>optional string plugin_id = 2;</code>
			 *
			 * <pre>
			 * for foreign users
			 * </pre>
			 */
			java.lang.String getPluginId();

			/**
			 * <code>optional string plugin_id = 2;</code>
			 *
			 * <pre>
			 * for foreign users
			 * </pre>
			 */
			com.google.protobuf.ByteString getPluginIdBytes();

			// optional .UserStatus user_status = 101;
			/**
			 * <code>optional .UserStatus user_status = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			boolean hasUserStatus();

			/**
			 * <code>optional .UserStatus user_status = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			BridgeChatProtoBuf.UserStatus getUserStatus();

			/**
			 * <code>optional .UserStatus user_status = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			BridgeChatProtoBuf.UserStatusOrBuilder getUserStatusOrBuilder();

			// optional string chat_message = 102;
			/**
			 * <code>optional string chat_message = 102;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			boolean hasChatMessage();

			/**
			 * <code>optional string chat_message = 102;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			java.lang.String getChatMessage();

			/**
			 * <code>optional string chat_message = 102;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			com.google.protobuf.ByteString getChatMessageBytes();
		}

		/**
		 * Protobuf type {@code UserEvent}
		 */
		public static final class UserEvent extends
				com.google.protobuf.GeneratedMessage implements
				UserEventOrBuilder {
			// Use UserEvent.newBuilder() to construct.
			private UserEvent(
					com.google.protobuf.GeneratedMessage.Builder<?> builder) {
				super(builder);
				this.unknownFields = builder.getUnknownFields();
			}

			private UserEvent(boolean noInit) {
				this.unknownFields = com.google.protobuf.UnknownFieldSet
						.getDefaultInstance();
			}

			private static final UserEvent defaultInstance;

			public static UserEvent getDefaultInstance() {
				return defaultInstance;
			}

			public UserEvent getDefaultInstanceForType() {
				return defaultInstance;
			}

			private final com.google.protobuf.UnknownFieldSet unknownFields;

			@java.lang.Override
			public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
				return this.unknownFields;
			}

			private UserEvent(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				initFields();
				com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
						.newBuilder();
				try {
					boolean done = false;
					while (!done) {
						int tag = input.readTag();
						switch (tag) {
						case 0:
							done = true;
							break;
						default: {
							if (!parseUnknownField(input, unknownFields,
									extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
						case 10: {
							bitField0_ |= 0x00000001;
							username_ = input.readBytes();
							break;
						}
						case 18: {
							bitField0_ |= 0x00000002;
							pluginId_ = input.readBytes();
							break;
						}
						case 810: {
							BridgeChatProtoBuf.UserStatus.Builder subBuilder = null;
							if (((bitField0_ & 0x00000004) == 0x00000004)) {
								subBuilder = userStatus_.toBuilder();
							}
							userStatus_ = input.readMessage(
									BridgeChatProtoBuf.UserStatus.PARSER,
									extensionRegistry);
							if (subBuilder != null) {
								subBuilder.mergeFrom(userStatus_);
								userStatus_ = subBuilder.buildPartial();
							}
							bitField0_ |= 0x00000004;
							break;
						}
						case 818: {
							bitField0_ |= 0x00000008;
							chatMessage_ = input.readBytes();
							break;
						}
						}
					}
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					throw e.setUnfinishedMessage(this);
				} catch (java.io.IOException e) {
					throw new com.google.protobuf.InvalidProtocolBufferException(
							e.getMessage()).setUnfinishedMessage(this);
				} finally {
					this.unknownFields = unknownFields.build();
					makeExtensionsImmutable();
				}
			}

			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return BridgeChatProtoBuf.internal_static_UserEvent_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return BridgeChatProtoBuf.internal_static_UserEvent_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								BridgeChatProtoBuf.UserEvent.class,
								BridgeChatProtoBuf.UserEvent.Builder.class);
			}

			public static com.google.protobuf.Parser<UserEvent> PARSER = new com.google.protobuf.AbstractParser<UserEvent>() {
				public UserEvent parsePartialFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws com.google.protobuf.InvalidProtocolBufferException {
					return new UserEvent(input, extensionRegistry);
				}
			};

			@java.lang.Override
			public com.google.protobuf.Parser<UserEvent> getParserForType() {
				return PARSER;
			}

			private int bitField0_;
			// required string username = 1;
			public static final int USERNAME_FIELD_NUMBER = 1;
			private java.lang.Object username_;

			/**
			 * <code>required string username = 1;</code>
			 */
			public boolean hasUsername() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			/**
			 * <code>required string username = 1;</code>
			 */
			public java.lang.String getUsername() {
				java.lang.Object ref = username_;
				if (ref instanceof java.lang.String) {
					return (java.lang.String) ref;
				} else {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						username_ = s;
					}
					return s;
				}
			}

			/**
			 * <code>required string username = 1;</code>
			 */
			public com.google.protobuf.ByteString getUsernameBytes() {
				java.lang.Object ref = username_;
				if (ref instanceof java.lang.String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					username_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			// optional string plugin_id = 2;
			public static final int PLUGIN_ID_FIELD_NUMBER = 2;
			private java.lang.Object pluginId_;

			/**
			 * <code>optional string plugin_id = 2;</code>
			 *
			 * <pre>
			 * for foreign users
			 * </pre>
			 */
			public boolean hasPluginId() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			/**
			 * <code>optional string plugin_id = 2;</code>
			 *
			 * <pre>
			 * for foreign users
			 * </pre>
			 */
			public java.lang.String getPluginId() {
				java.lang.Object ref = pluginId_;
				if (ref instanceof java.lang.String) {
					return (java.lang.String) ref;
				} else {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						pluginId_ = s;
					}
					return s;
				}
			}

			/**
			 * <code>optional string plugin_id = 2;</code>
			 *
			 * <pre>
			 * for foreign users
			 * </pre>
			 */
			public com.google.protobuf.ByteString getPluginIdBytes() {
				java.lang.Object ref = pluginId_;
				if (ref instanceof java.lang.String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					pluginId_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			// optional .UserStatus user_status = 101;
			public static final int USER_STATUS_FIELD_NUMBER = 101;
			private BridgeChatProtoBuf.UserStatus userStatus_;

			/**
			 * <code>optional .UserStatus user_status = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			public boolean hasUserStatus() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			/**
			 * <code>optional .UserStatus user_status = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			public BridgeChatProtoBuf.UserStatus getUserStatus() {
				return userStatus_;
			}

			/**
			 * <code>optional .UserStatus user_status = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			public BridgeChatProtoBuf.UserStatusOrBuilder getUserStatusOrBuilder() {
				return userStatus_;
			}

			// optional string chat_message = 102;
			public static final int CHAT_MESSAGE_FIELD_NUMBER = 102;
			private java.lang.Object chatMessage_;

			/**
			 * <code>optional string chat_message = 102;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			public boolean hasChatMessage() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			/**
			 * <code>optional string chat_message = 102;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			public java.lang.String getChatMessage() {
				java.lang.Object ref = chatMessage_;
				if (ref instanceof java.lang.String) {
					return (java.lang.String) ref;
				} else {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						chatMessage_ = s;
					}
					return s;
				}
			}

			/**
			 * <code>optional string chat_message = 102;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			public com.google.protobuf.ByteString getChatMessageBytes() {
				java.lang.Object ref = chatMessage_;
				if (ref instanceof java.lang.String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					chatMessage_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			private void initFields() {
				username_ = "";
				pluginId_ = "";
				userStatus_ = BridgeChatProtoBuf.UserStatus
						.getDefaultInstance();
				chatMessage_ = "";
			}

			private byte memoizedIsInitialized = -1;

			public final boolean isInitialized() {
				byte isInitialized = memoizedIsInitialized;
				if (isInitialized != -1)
					return isInitialized == 1;

				if (!hasUsername()) {
					memoizedIsInitialized = 0;
					return false;
				}
				memoizedIsInitialized = 1;
				return true;
			}

			public void writeTo(com.google.protobuf.CodedOutputStream output)
					throws java.io.IOException {
				getSerializedSize();
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					output.writeBytes(1, getUsernameBytes());
				}
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					output.writeBytes(2, getPluginIdBytes());
				}
				if (((bitField0_ & 0x00000004) == 0x00000004)) {
					output.writeMessage(101, userStatus_);
				}
				if (((bitField0_ & 0x00000008) == 0x00000008)) {
					output.writeBytes(102, getChatMessageBytes());
				}
				getUnknownFields().writeTo(output);
			}

			private int memoizedSerializedSize = -1;

			public int getSerializedSize() {
				int size = memoizedSerializedSize;
				if (size != -1)
					return size;

				size = 0;
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBytesSize(1, getUsernameBytes());
				}
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBytesSize(2, getPluginIdBytes());
				}
				if (((bitField0_ & 0x00000004) == 0x00000004)) {
					size += com.google.protobuf.CodedOutputStream
							.computeMessageSize(101, userStatus_);
				}
				if (((bitField0_ & 0x00000008) == 0x00000008)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBytesSize(102, getChatMessageBytes());
				}
				size += getUnknownFields().getSerializedSize();
				memoizedSerializedSize = size;
				return size;
			}

			private static final long serialVersionUID = 0L;

			@java.lang.Override
			protected java.lang.Object writeReplace()
					throws java.io.ObjectStreamException {
				return super.writeReplace();
			}

			public static BridgeChatProtoBuf.UserEvent parseFrom(
					com.google.protobuf.ByteString data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.UserEvent parseFrom(
					com.google.protobuf.ByteString data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UserEvent parseFrom(byte[] data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.UserEvent parseFrom(byte[] data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UserEvent parseFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.UserEvent parseFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UserEvent parseDelimitedFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input);
			}

			public static BridgeChatProtoBuf.UserEvent parseDelimitedFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.UserEvent parseFrom(
					com.google.protobuf.CodedInputStream input)
					throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.UserEvent parseFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static Builder newBuilder() {
				return Builder.create();
			}

			public Builder newBuilderForType() {
				return newBuilder();
			}

			public static Builder newBuilder(
					BridgeChatProtoBuf.UserEvent prototype) {
				return newBuilder().mergeFrom(prototype);
			}

			public Builder toBuilder() {
				return newBuilder(this);
			}

			@java.lang.Override
			protected Builder newBuilderForType(
					com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				Builder builder = new Builder(parent);
				return builder;
			}

			/**
			 * Protobuf type {@code UserEvent}
			 */
			public static final class Builder extends
					com.google.protobuf.GeneratedMessage.Builder<Builder>
					implements BridgeChatProtoBuf.UserEventOrBuilder {
				public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
					return BridgeChatProtoBuf.internal_static_UserEvent_descriptor;
				}

				protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
					return BridgeChatProtoBuf.internal_static_UserEvent_fieldAccessorTable
							.ensureFieldAccessorsInitialized(
									BridgeChatProtoBuf.UserEvent.class,
									BridgeChatProtoBuf.UserEvent.Builder.class);
				}

				// Construct using BridgeChat.UserEvent.newBuilder()
				private Builder() {
					maybeForceBuilderInitialization();
				}

				private Builder(
						com.google.protobuf.GeneratedMessage.BuilderParent parent) {
					super(parent);
					maybeForceBuilderInitialization();
				}

				private void maybeForceBuilderInitialization() {
					if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
						getUserStatusFieldBuilder();
					}
				}

				private static Builder create() {
					return new Builder();
				}

				public Builder clear() {
					super.clear();
					username_ = "";
					bitField0_ = (bitField0_ & ~0x00000001);
					pluginId_ = "";
					bitField0_ = (bitField0_ & ~0x00000002);
					if (userStatusBuilder_ == null) {
						userStatus_ = BridgeChatProtoBuf.UserStatus
								.getDefaultInstance();
					} else {
						userStatusBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000004);
					chatMessage_ = "";
					bitField0_ = (bitField0_ & ~0x00000008);
					return this;
				}

				public Builder clone() {
					return create().mergeFrom(buildPartial());
				}

				public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
					return BridgeChatProtoBuf.internal_static_UserEvent_descriptor;
				}

				public BridgeChatProtoBuf.UserEvent getDefaultInstanceForType() {
					return BridgeChatProtoBuf.UserEvent.getDefaultInstance();
				}

				public BridgeChatProtoBuf.UserEvent build() {
					BridgeChatProtoBuf.UserEvent result = buildPartial();
					if (!result.isInitialized()) {
						throw newUninitializedMessageException(result);
					}
					return result;
				}

				public BridgeChatProtoBuf.UserEvent buildPartial() {
					BridgeChatProtoBuf.UserEvent result = new BridgeChatProtoBuf.UserEvent(
							this);
					int from_bitField0_ = bitField0_;
					int to_bitField0_ = 0;
					if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
						to_bitField0_ |= 0x00000001;
					}
					result.username_ = username_;
					if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
						to_bitField0_ |= 0x00000002;
					}
					result.pluginId_ = pluginId_;
					if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
						to_bitField0_ |= 0x00000004;
					}
					if (userStatusBuilder_ == null) {
						result.userStatus_ = userStatus_;
					} else {
						result.userStatus_ = userStatusBuilder_.build();
					}
					if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
						to_bitField0_ |= 0x00000008;
					}
					result.chatMessage_ = chatMessage_;
					result.bitField0_ = to_bitField0_;
					onBuilt();
					return result;
				}

				public Builder mergeFrom(com.google.protobuf.Message other) {
					if (other instanceof BridgeChatProtoBuf.UserEvent) {
						return mergeFrom((BridgeChatProtoBuf.UserEvent) other);
					} else {
						super.mergeFrom(other);
						return this;
					}
				}

				public Builder mergeFrom(BridgeChatProtoBuf.UserEvent other) {
					if (other == BridgeChatProtoBuf.UserEvent
							.getDefaultInstance())
						return this;
					if (other.hasUsername()) {
						bitField0_ |= 0x00000001;
						username_ = other.username_;
						onChanged();
					}
					if (other.hasPluginId()) {
						bitField0_ |= 0x00000002;
						pluginId_ = other.pluginId_;
						onChanged();
					}
					if (other.hasUserStatus()) {
						mergeUserStatus(other.getUserStatus());
					}
					if (other.hasChatMessage()) {
						bitField0_ |= 0x00000008;
						chatMessage_ = other.chatMessage_;
						onChanged();
					}
					this.mergeUnknownFields(other.getUnknownFields());
					return this;
				}

				public final boolean isInitialized() {
					if (!hasUsername()) {

						return false;
					}
					return true;
				}

				public Builder mergeFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws java.io.IOException {
					BridgeChatProtoBuf.UserEvent parsedMessage = null;
					try {
						parsedMessage = PARSER.parsePartialFrom(input,
								extensionRegistry);
					} catch (com.google.protobuf.InvalidProtocolBufferException e) {
						parsedMessage = (BridgeChatProtoBuf.UserEvent) e
								.getUnfinishedMessage();
						throw e;
					} finally {
						if (parsedMessage != null) {
							mergeFrom(parsedMessage);
						}
					}
					return this;
				}

				private int bitField0_;

				// required string username = 1;
				private java.lang.Object username_ = "";

				/**
				 * <code>required string username = 1;</code>
				 */
				public boolean hasUsername() {
					return ((bitField0_ & 0x00000001) == 0x00000001);
				}

				/**
				 * <code>required string username = 1;</code>
				 */
				public java.lang.String getUsername() {
					java.lang.Object ref = username_;
					if (!(ref instanceof java.lang.String)) {
						java.lang.String s = ((com.google.protobuf.ByteString) ref)
								.toStringUtf8();
						username_ = s;
						return s;
					} else {
						return (java.lang.String) ref;
					}
				}

				/**
				 * <code>required string username = 1;</code>
				 */
				public com.google.protobuf.ByteString getUsernameBytes() {
					java.lang.Object ref = username_;
					if (ref instanceof String) {
						com.google.protobuf.ByteString b = com.google.protobuf.ByteString
								.copyFromUtf8((java.lang.String) ref);
						username_ = b;
						return b;
					} else {
						return (com.google.protobuf.ByteString) ref;
					}
				}

				/**
				 * <code>required string username = 1;</code>
				 */
				public Builder setUsername(java.lang.String value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000001;
					username_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>required string username = 1;</code>
				 */
				public Builder clearUsername() {
					bitField0_ = (bitField0_ & ~0x00000001);
					username_ = getDefaultInstance().getUsername();
					onChanged();
					return this;
				}

				/**
				 * <code>required string username = 1;</code>
				 */
				public Builder setUsernameBytes(
						com.google.protobuf.ByteString value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000001;
					username_ = value;
					onChanged();
					return this;
				}

				// optional string plugin_id = 2;
				private java.lang.Object pluginId_ = "";

				/**
				 * <code>optional string plugin_id = 2;</code>
				 *
				 * <pre>
				 * for foreign users
				 * </pre>
				 */
				public boolean hasPluginId() {
					return ((bitField0_ & 0x00000002) == 0x00000002);
				}

				/**
				 * <code>optional string plugin_id = 2;</code>
				 *
				 * <pre>
				 * for foreign users
				 * </pre>
				 */
				public java.lang.String getPluginId() {
					java.lang.Object ref = pluginId_;
					if (!(ref instanceof java.lang.String)) {
						java.lang.String s = ((com.google.protobuf.ByteString) ref)
								.toStringUtf8();
						pluginId_ = s;
						return s;
					} else {
						return (java.lang.String) ref;
					}
				}

				/**
				 * <code>optional string plugin_id = 2;</code>
				 *
				 * <pre>
				 * for foreign users
				 * </pre>
				 */
				public com.google.protobuf.ByteString getPluginIdBytes() {
					java.lang.Object ref = pluginId_;
					if (ref instanceof String) {
						com.google.protobuf.ByteString b = com.google.protobuf.ByteString
								.copyFromUtf8((java.lang.String) ref);
						pluginId_ = b;
						return b;
					} else {
						return (com.google.protobuf.ByteString) ref;
					}
				}

				/**
				 * <code>optional string plugin_id = 2;</code>
				 *
				 * <pre>
				 * for foreign users
				 * </pre>
				 */
				public Builder setPluginId(java.lang.String value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000002;
					pluginId_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>optional string plugin_id = 2;</code>
				 *
				 * <pre>
				 * for foreign users
				 * </pre>
				 */
				public Builder clearPluginId() {
					bitField0_ = (bitField0_ & ~0x00000002);
					pluginId_ = getDefaultInstance().getPluginId();
					onChanged();
					return this;
				}

				/**
				 * <code>optional string plugin_id = 2;</code>
				 *
				 * <pre>
				 * for foreign users
				 * </pre>
				 */
				public Builder setPluginIdBytes(
						com.google.protobuf.ByteString value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000002;
					pluginId_ = value;
					onChanged();
					return this;
				}

				// optional .UserStatus user_status = 101;
				private BridgeChatProtoBuf.UserStatus userStatus_ = BridgeChatProtoBuf.UserStatus
						.getDefaultInstance();
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UserStatus, BridgeChatProtoBuf.UserStatus.Builder, BridgeChatProtoBuf.UserStatusOrBuilder> userStatusBuilder_;

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public boolean hasUserStatus() {
					return ((bitField0_ & 0x00000004) == 0x00000004);
				}

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public BridgeChatProtoBuf.UserStatus getUserStatus() {
					if (userStatusBuilder_ == null) {
						return userStatus_;
					} else {
						return userStatusBuilder_.getMessage();
					}
				}

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public Builder setUserStatus(BridgeChatProtoBuf.UserStatus value) {
					if (userStatusBuilder_ == null) {
						if (value == null) {
							throw new NullPointerException();
						}
						userStatus_ = value;
						onChanged();
					} else {
						userStatusBuilder_.setMessage(value);
					}
					bitField0_ |= 0x00000004;
					return this;
				}

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public Builder setUserStatus(
						BridgeChatProtoBuf.UserStatus.Builder builderForValue) {
					if (userStatusBuilder_ == null) {
						userStatus_ = builderForValue.build();
						onChanged();
					} else {
						userStatusBuilder_.setMessage(builderForValue.build());
					}
					bitField0_ |= 0x00000004;
					return this;
				}

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public Builder mergeUserStatus(
						BridgeChatProtoBuf.UserStatus value) {
					if (userStatusBuilder_ == null) {
						if (((bitField0_ & 0x00000004) == 0x00000004)
								&& userStatus_ != BridgeChatProtoBuf.UserStatus
										.getDefaultInstance()) {
							userStatus_ = BridgeChatProtoBuf.UserStatus
									.newBuilder(userStatus_).mergeFrom(value)
									.buildPartial();
						} else {
							userStatus_ = value;
						}
						onChanged();
					} else {
						userStatusBuilder_.mergeFrom(value);
					}
					bitField0_ |= 0x00000004;
					return this;
				}

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public Builder clearUserStatus() {
					if (userStatusBuilder_ == null) {
						userStatus_ = BridgeChatProtoBuf.UserStatus
								.getDefaultInstance();
						onChanged();
					} else {
						userStatusBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000004);
					return this;
				}

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public BridgeChatProtoBuf.UserStatus.Builder getUserStatusBuilder() {
					bitField0_ |= 0x00000004;
					onChanged();
					return getUserStatusFieldBuilder().getBuilder();
				}

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public BridgeChatProtoBuf.UserStatusOrBuilder getUserStatusOrBuilder() {
					if (userStatusBuilder_ != null) {
						return userStatusBuilder_.getMessageOrBuilder();
					} else {
						return userStatus_;
					}
				}

				/**
				 * <code>optional .UserStatus user_status = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UserStatus, BridgeChatProtoBuf.UserStatus.Builder, BridgeChatProtoBuf.UserStatusOrBuilder> getUserStatusFieldBuilder() {
					if (userStatusBuilder_ == null) {
						userStatusBuilder_ = new com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UserStatus, BridgeChatProtoBuf.UserStatus.Builder, BridgeChatProtoBuf.UserStatusOrBuilder>(
								userStatus_, getParentForChildren(), isClean());
						userStatus_ = null;
					}
					return userStatusBuilder_;
				}

				// optional string chat_message = 102;
				private java.lang.Object chatMessage_ = "";

				/**
				 * <code>optional string chat_message = 102;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public boolean hasChatMessage() {
					return ((bitField0_ & 0x00000008) == 0x00000008);
				}

				/**
				 * <code>optional string chat_message = 102;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public java.lang.String getChatMessage() {
					java.lang.Object ref = chatMessage_;
					if (!(ref instanceof java.lang.String)) {
						java.lang.String s = ((com.google.protobuf.ByteString) ref)
								.toStringUtf8();
						chatMessage_ = s;
						return s;
					} else {
						return (java.lang.String) ref;
					}
				}

				/**
				 * <code>optional string chat_message = 102;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public com.google.protobuf.ByteString getChatMessageBytes() {
					java.lang.Object ref = chatMessage_;
					if (ref instanceof String) {
						com.google.protobuf.ByteString b = com.google.protobuf.ByteString
								.copyFromUtf8((java.lang.String) ref);
						chatMessage_ = b;
						return b;
					} else {
						return (com.google.protobuf.ByteString) ref;
					}
				}

				/**
				 * <code>optional string chat_message = 102;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public Builder setChatMessage(java.lang.String value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000008;
					chatMessage_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>optional string chat_message = 102;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public Builder clearChatMessage() {
					bitField0_ = (bitField0_ & ~0x00000008);
					chatMessage_ = getDefaultInstance().getChatMessage();
					onChanged();
					return this;
				}

				/**
				 * <code>optional string chat_message = 102;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public Builder setChatMessageBytes(
						com.google.protobuf.ByteString value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000008;
					chatMessage_ = value;
					onChanged();
					return this;
				}

				// @@protoc_insertion_point(builder_scope:UserEvent)
			}

			static {
				defaultInstance = new UserEvent(true);
				defaultInstance.initFields();
			}

			// @@protoc_insertion_point(class_scope:UserEvent)
		}

		public interface GroupStatusOrBuilder extends
				com.google.protobuf.MessageOrBuilder {

			// optional string topic = 1;
			/**
			 * <code>optional string topic = 1;</code>
			 */
			boolean hasTopic();

			/**
			 * <code>optional string topic = 1;</code>
			 */
			java.lang.String getTopic();

			/**
			 * <code>optional string topic = 1;</code>
			 */
			com.google.protobuf.ByteString getTopicBytes();
		}

		/**
		 * Protobuf type {@code GroupStatus}
		 */
		public static final class GroupStatus extends
				com.google.protobuf.GeneratedMessage implements
				GroupStatusOrBuilder {
			// Use GroupStatus.newBuilder() to construct.
			private GroupStatus(
					com.google.protobuf.GeneratedMessage.Builder<?> builder) {
				super(builder);
				this.unknownFields = builder.getUnknownFields();
			}

			private GroupStatus(boolean noInit) {
				this.unknownFields = com.google.protobuf.UnknownFieldSet
						.getDefaultInstance();
			}

			private static final GroupStatus defaultInstance;

			public static GroupStatus getDefaultInstance() {
				return defaultInstance;
			}

			public GroupStatus getDefaultInstanceForType() {
				return defaultInstance;
			}

			private final com.google.protobuf.UnknownFieldSet unknownFields;

			@java.lang.Override
			public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
				return this.unknownFields;
			}

			private GroupStatus(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				initFields();
				com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
						.newBuilder();
				try {
					boolean done = false;
					while (!done) {
						int tag = input.readTag();
						switch (tag) {
						case 0:
							done = true;
							break;
						default: {
							if (!parseUnknownField(input, unknownFields,
									extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
						case 10: {
							bitField0_ |= 0x00000001;
							topic_ = input.readBytes();
							break;
						}
						}
					}
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					throw e.setUnfinishedMessage(this);
				} catch (java.io.IOException e) {
					throw new com.google.protobuf.InvalidProtocolBufferException(
							e.getMessage()).setUnfinishedMessage(this);
				} finally {
					this.unknownFields = unknownFields.build();
					makeExtensionsImmutable();
				}
			}

			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return BridgeChatProtoBuf.internal_static_GroupStatus_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return BridgeChatProtoBuf.internal_static_GroupStatus_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								BridgeChatProtoBuf.GroupStatus.class,
								BridgeChatProtoBuf.GroupStatus.Builder.class);
			}

			public static com.google.protobuf.Parser<GroupStatus> PARSER = new com.google.protobuf.AbstractParser<GroupStatus>() {
				public GroupStatus parsePartialFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws com.google.protobuf.InvalidProtocolBufferException {
					return new GroupStatus(input, extensionRegistry);
				}
			};

			@java.lang.Override
			public com.google.protobuf.Parser<GroupStatus> getParserForType() {
				return PARSER;
			}

			private int bitField0_;
			// optional string topic = 1;
			public static final int TOPIC_FIELD_NUMBER = 1;
			private java.lang.Object topic_;

			/**
			 * <code>optional string topic = 1;</code>
			 */
			public boolean hasTopic() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			/**
			 * <code>optional string topic = 1;</code>
			 */
			public java.lang.String getTopic() {
				java.lang.Object ref = topic_;
				if (ref instanceof java.lang.String) {
					return (java.lang.String) ref;
				} else {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						topic_ = s;
					}
					return s;
				}
			}

			/**
			 * <code>optional string topic = 1;</code>
			 */
			public com.google.protobuf.ByteString getTopicBytes() {
				java.lang.Object ref = topic_;
				if (ref instanceof java.lang.String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					topic_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			private void initFields() {
				topic_ = "";
			}

			private byte memoizedIsInitialized = -1;

			public final boolean isInitialized() {
				byte isInitialized = memoizedIsInitialized;
				if (isInitialized != -1)
					return isInitialized == 1;

				memoizedIsInitialized = 1;
				return true;
			}

			public void writeTo(com.google.protobuf.CodedOutputStream output)
					throws java.io.IOException {
				getSerializedSize();
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					output.writeBytes(1, getTopicBytes());
				}
				getUnknownFields().writeTo(output);
			}

			private int memoizedSerializedSize = -1;

			public int getSerializedSize() {
				int size = memoizedSerializedSize;
				if (size != -1)
					return size;

				size = 0;
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					size += com.google.protobuf.CodedOutputStream
							.computeBytesSize(1, getTopicBytes());
				}
				size += getUnknownFields().getSerializedSize();
				memoizedSerializedSize = size;
				return size;
			}

			private static final long serialVersionUID = 0L;

			@java.lang.Override
			protected java.lang.Object writeReplace()
					throws java.io.ObjectStreamException {
				return super.writeReplace();
			}

			public static BridgeChatProtoBuf.GroupStatus parseFrom(
					com.google.protobuf.ByteString data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.GroupStatus parseFrom(
					com.google.protobuf.ByteString data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.GroupStatus parseFrom(byte[] data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.GroupStatus parseFrom(byte[] data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.GroupStatus parseFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.GroupStatus parseFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.GroupStatus parseDelimitedFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input);
			}

			public static BridgeChatProtoBuf.GroupStatus parseDelimitedFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.GroupStatus parseFrom(
					com.google.protobuf.CodedInputStream input)
					throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.GroupStatus parseFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static Builder newBuilder() {
				return Builder.create();
			}

			public Builder newBuilderForType() {
				return newBuilder();
			}

			public static Builder newBuilder(
					BridgeChatProtoBuf.GroupStatus prototype) {
				return newBuilder().mergeFrom(prototype);
			}

			public Builder toBuilder() {
				return newBuilder(this);
			}

			@java.lang.Override
			protected Builder newBuilderForType(
					com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				Builder builder = new Builder(parent);
				return builder;
			}

			/**
			 * Protobuf type {@code GroupStatus}
			 */
			public static final class Builder extends
					com.google.protobuf.GeneratedMessage.Builder<Builder>
					implements BridgeChatProtoBuf.GroupStatusOrBuilder {
				public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
					return BridgeChatProtoBuf.internal_static_GroupStatus_descriptor;
				}

				protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
					return BridgeChatProtoBuf.internal_static_GroupStatus_fieldAccessorTable
							.ensureFieldAccessorsInitialized(
									BridgeChatProtoBuf.GroupStatus.class,
									BridgeChatProtoBuf.GroupStatus.Builder.class);
				}

				// Construct using BridgeChat.GroupStatus.newBuilder()
				private Builder() {
					maybeForceBuilderInitialization();
				}

				private Builder(
						com.google.protobuf.GeneratedMessage.BuilderParent parent) {
					super(parent);
					maybeForceBuilderInitialization();
				}

				private void maybeForceBuilderInitialization() {
					if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					}
				}

				private static Builder create() {
					return new Builder();
				}

				public Builder clear() {
					super.clear();
					topic_ = "";
					bitField0_ = (bitField0_ & ~0x00000001);
					return this;
				}

				public Builder clone() {
					return create().mergeFrom(buildPartial());
				}

				public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
					return BridgeChatProtoBuf.internal_static_GroupStatus_descriptor;
				}

				public BridgeChatProtoBuf.GroupStatus getDefaultInstanceForType() {
					return BridgeChatProtoBuf.GroupStatus.getDefaultInstance();
				}

				public BridgeChatProtoBuf.GroupStatus build() {
					BridgeChatProtoBuf.GroupStatus result = buildPartial();
					if (!result.isInitialized()) {
						throw newUninitializedMessageException(result);
					}
					return result;
				}

				public BridgeChatProtoBuf.GroupStatus buildPartial() {
					BridgeChatProtoBuf.GroupStatus result = new BridgeChatProtoBuf.GroupStatus(
							this);
					int from_bitField0_ = bitField0_;
					int to_bitField0_ = 0;
					if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
						to_bitField0_ |= 0x00000001;
					}
					result.topic_ = topic_;
					result.bitField0_ = to_bitField0_;
					onBuilt();
					return result;
				}

				public Builder mergeFrom(com.google.protobuf.Message other) {
					if (other instanceof BridgeChatProtoBuf.GroupStatus) {
						return mergeFrom((BridgeChatProtoBuf.GroupStatus) other);
					} else {
						super.mergeFrom(other);
						return this;
					}
				}

				public Builder mergeFrom(BridgeChatProtoBuf.GroupStatus other) {
					if (other == BridgeChatProtoBuf.GroupStatus
							.getDefaultInstance())
						return this;
					if (other.hasTopic()) {
						bitField0_ |= 0x00000001;
						topic_ = other.topic_;
						onChanged();
					}
					this.mergeUnknownFields(other.getUnknownFields());
					return this;
				}

				public final boolean isInitialized() {
					return true;
				}

				public Builder mergeFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws java.io.IOException {
					BridgeChatProtoBuf.GroupStatus parsedMessage = null;
					try {
						parsedMessage = PARSER.parsePartialFrom(input,
								extensionRegistry);
					} catch (com.google.protobuf.InvalidProtocolBufferException e) {
						parsedMessage = (BridgeChatProtoBuf.GroupStatus) e
								.getUnfinishedMessage();
						throw e;
					} finally {
						if (parsedMessage != null) {
							mergeFrom(parsedMessage);
						}
					}
					return this;
				}

				private int bitField0_;

				// optional string topic = 1;
				private java.lang.Object topic_ = "";

				/**
				 * <code>optional string topic = 1;</code>
				 */
				public boolean hasTopic() {
					return ((bitField0_ & 0x00000001) == 0x00000001);
				}

				/**
				 * <code>optional string topic = 1;</code>
				 */
				public java.lang.String getTopic() {
					java.lang.Object ref = topic_;
					if (!(ref instanceof java.lang.String)) {
						java.lang.String s = ((com.google.protobuf.ByteString) ref)
								.toStringUtf8();
						topic_ = s;
						return s;
					} else {
						return (java.lang.String) ref;
					}
				}

				/**
				 * <code>optional string topic = 1;</code>
				 */
				public com.google.protobuf.ByteString getTopicBytes() {
					java.lang.Object ref = topic_;
					if (ref instanceof String) {
						com.google.protobuf.ByteString b = com.google.protobuf.ByteString
								.copyFromUtf8((java.lang.String) ref);
						topic_ = b;
						return b;
					} else {
						return (com.google.protobuf.ByteString) ref;
					}
				}

				/**
				 * <code>optional string topic = 1;</code>
				 */
				public Builder setTopic(java.lang.String value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000001;
					topic_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>optional string topic = 1;</code>
				 */
				public Builder clearTopic() {
					bitField0_ = (bitField0_ & ~0x00000001);
					topic_ = getDefaultInstance().getTopic();
					onChanged();
					return this;
				}

				/**
				 * <code>optional string topic = 1;</code>
				 */
				public Builder setTopicBytes(
						com.google.protobuf.ByteString value) {
					if (value == null) {
						throw new NullPointerException();
					}
					bitField0_ |= 0x00000001;
					topic_ = value;
					onChanged();
					return this;
				}

				// @@protoc_insertion_point(builder_scope:GroupStatus)
			}

			static {
				defaultInstance = new GroupStatus(true);
				defaultInstance.initFields();
			}

			// @@protoc_insertion_point(class_scope:GroupStatus)
		}

		public interface GroupMessageOrBuilder extends
				com.google.protobuf.MessageOrBuilder {

			// required uint32 group_id = 1;
			/**
			 * <code>required uint32 group_id = 1;</code>
			 */
			boolean hasGroupId();

			/**
			 * <code>required uint32 group_id = 1;</code>
			 */
			int getGroupId();

			// optional .BindingRequest binding_request = 101;
			/**
			 * <code>optional .BindingRequest binding_request = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			boolean hasBindingRequest();

			/**
			 * <code>optional .BindingRequest binding_request = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			BridgeChatProtoBuf.BindingRequest getBindingRequest();

			/**
			 * <code>optional .BindingRequest binding_request = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			BridgeChatProtoBuf.BindingRequestOrBuilder getBindingRequestOrBuilder();

			// optional .BindingResponse binding_response = 102;
			/**
			 * <code>optional .BindingResponse binding_response = 102;</code>
			 */
			boolean hasBindingResponse();

			/**
			 * <code>optional .BindingResponse binding_response = 102;</code>
			 */
			BridgeChatProtoBuf.BindingResponse getBindingResponse();

			/**
			 * <code>optional .BindingResponse binding_response = 102;</code>
			 */
			BridgeChatProtoBuf.BindingResponseOrBuilder getBindingResponseOrBuilder();

			// optional .UnbindRequest unbind_request = 103;
			/**
			 * <code>optional .UnbindRequest unbind_request = 103;</code>
			 */
			boolean hasUnbindRequest();

			/**
			 * <code>optional .UnbindRequest unbind_request = 103;</code>
			 */
			BridgeChatProtoBuf.UnbindRequest getUnbindRequest();

			/**
			 * <code>optional .UnbindRequest unbind_request = 103;</code>
			 */
			BridgeChatProtoBuf.UnbindRequestOrBuilder getUnbindRequestOrBuilder();

			// optional .GroupStatus group_status_change = 104;
			/**
			 * <code>optional .GroupStatus group_status_change = 104;</code>
			 */
			boolean hasGroupStatusChange();

			/**
			 * <code>optional .GroupStatus group_status_change = 104;</code>
			 */
			BridgeChatProtoBuf.GroupStatus getGroupStatusChange();

			/**
			 * <code>optional .GroupStatus group_status_change = 104;</code>
			 */
			BridgeChatProtoBuf.GroupStatusOrBuilder getGroupStatusChangeOrBuilder();

			// optional .UserEvent user_event = 105;
			/**
			 * <code>optional .UserEvent user_event = 105;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			boolean hasUserEvent();

			/**
			 * <code>optional .UserEvent user_event = 105;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			BridgeChatProtoBuf.UserEvent getUserEvent();

			/**
			 * <code>optional .UserEvent user_event = 105;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			BridgeChatProtoBuf.UserEventOrBuilder getUserEventOrBuilder();
		}

		/**
		 * Protobuf type {@code GroupMessage}
		 */
		public static final class GroupMessage extends
				com.google.protobuf.GeneratedMessage implements
				GroupMessageOrBuilder {
			// Use GroupMessage.newBuilder() to construct.
			private GroupMessage(
					com.google.protobuf.GeneratedMessage.Builder<?> builder) {
				super(builder);
				this.unknownFields = builder.getUnknownFields();
			}

			private GroupMessage(boolean noInit) {
				this.unknownFields = com.google.protobuf.UnknownFieldSet
						.getDefaultInstance();
			}

			private static final GroupMessage defaultInstance;

			public static GroupMessage getDefaultInstance() {
				return defaultInstance;
			}

			public GroupMessage getDefaultInstanceForType() {
				return defaultInstance;
			}

			private final com.google.protobuf.UnknownFieldSet unknownFields;

			@java.lang.Override
			public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
				return this.unknownFields;
			}

			private GroupMessage(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				initFields();
				com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
						.newBuilder();
				try {
					boolean done = false;
					while (!done) {
						int tag = input.readTag();
						switch (tag) {
						case 0:
							done = true;
							break;
						default: {
							if (!parseUnknownField(input, unknownFields,
									extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
						case 8: {
							bitField0_ |= 0x00000001;
							groupId_ = input.readUInt32();
							break;
						}
						case 810: {
							BridgeChatProtoBuf.BindingRequest.Builder subBuilder = null;
							if (((bitField0_ & 0x00000002) == 0x00000002)) {
								subBuilder = bindingRequest_.toBuilder();
							}
							bindingRequest_ = input.readMessage(
									BridgeChatProtoBuf.BindingRequest.PARSER,
									extensionRegistry);
							if (subBuilder != null) {
								subBuilder.mergeFrom(bindingRequest_);
								bindingRequest_ = subBuilder.buildPartial();
							}
							bitField0_ |= 0x00000002;
							break;
						}
						case 818: {
							BridgeChatProtoBuf.BindingResponse.Builder subBuilder = null;
							if (((bitField0_ & 0x00000004) == 0x00000004)) {
								subBuilder = bindingResponse_.toBuilder();
							}
							bindingResponse_ = input.readMessage(
									BridgeChatProtoBuf.BindingResponse.PARSER,
									extensionRegistry);
							if (subBuilder != null) {
								subBuilder.mergeFrom(bindingResponse_);
								bindingResponse_ = subBuilder.buildPartial();
							}
							bitField0_ |= 0x00000004;
							break;
						}
						case 826: {
							BridgeChatProtoBuf.UnbindRequest.Builder subBuilder = null;
							if (((bitField0_ & 0x00000008) == 0x00000008)) {
								subBuilder = unbindRequest_.toBuilder();
							}
							unbindRequest_ = input.readMessage(
									BridgeChatProtoBuf.UnbindRequest.PARSER,
									extensionRegistry);
							if (subBuilder != null) {
								subBuilder.mergeFrom(unbindRequest_);
								unbindRequest_ = subBuilder.buildPartial();
							}
							bitField0_ |= 0x00000008;
							break;
						}
						case 834: {
							BridgeChatProtoBuf.GroupStatus.Builder subBuilder = null;
							if (((bitField0_ & 0x00000010) == 0x00000010)) {
								subBuilder = groupStatusChange_.toBuilder();
							}
							groupStatusChange_ = input.readMessage(
									BridgeChatProtoBuf.GroupStatus.PARSER,
									extensionRegistry);
							if (subBuilder != null) {
								subBuilder.mergeFrom(groupStatusChange_);
								groupStatusChange_ = subBuilder.buildPartial();
							}
							bitField0_ |= 0x00000010;
							break;
						}
						case 842: {
							BridgeChatProtoBuf.UserEvent.Builder subBuilder = null;
							if (((bitField0_ & 0x00000020) == 0x00000020)) {
								subBuilder = userEvent_.toBuilder();
							}
							userEvent_ = input.readMessage(
									BridgeChatProtoBuf.UserEvent.PARSER,
									extensionRegistry);
							if (subBuilder != null) {
								subBuilder.mergeFrom(userEvent_);
								userEvent_ = subBuilder.buildPartial();
							}
							bitField0_ |= 0x00000020;
							break;
						}
						}
					}
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					throw e.setUnfinishedMessage(this);
				} catch (java.io.IOException e) {
					throw new com.google.protobuf.InvalidProtocolBufferException(
							e.getMessage()).setUnfinishedMessage(this);
				} finally {
					this.unknownFields = unknownFields.build();
					makeExtensionsImmutable();
				}
			}

			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return BridgeChatProtoBuf.internal_static_GroupMessage_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return BridgeChatProtoBuf.internal_static_GroupMessage_fieldAccessorTable
						.ensureFieldAccessorsInitialized(
								BridgeChatProtoBuf.GroupMessage.class,
								BridgeChatProtoBuf.GroupMessage.Builder.class);
			}

			public static com.google.protobuf.Parser<GroupMessage> PARSER = new com.google.protobuf.AbstractParser<GroupMessage>() {
				public GroupMessage parsePartialFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws com.google.protobuf.InvalidProtocolBufferException {
					return new GroupMessage(input, extensionRegistry);
				}
			};

			@java.lang.Override
			public com.google.protobuf.Parser<GroupMessage> getParserForType() {
				return PARSER;
			}

			private int bitField0_;
			// required uint32 group_id = 1;
			public static final int GROUP_ID_FIELD_NUMBER = 1;
			private int groupId_;

			/**
			 * <code>required uint32 group_id = 1;</code>
			 */
			public boolean hasGroupId() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			/**
			 * <code>required uint32 group_id = 1;</code>
			 */
			public int getGroupId() {
				return groupId_;
			}

			// optional .BindingRequest binding_request = 101;
			public static final int BINDING_REQUEST_FIELD_NUMBER = 101;
			private BridgeChatProtoBuf.BindingRequest bindingRequest_;

			/**
			 * <code>optional .BindingRequest binding_request = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			public boolean hasBindingRequest() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			/**
			 * <code>optional .BindingRequest binding_request = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			public BridgeChatProtoBuf.BindingRequest getBindingRequest() {
				return bindingRequest_;
			}

			/**
			 * <code>optional .BindingRequest binding_request = 101;</code>
			 *
			 * <pre>
			 * oneof {
			 * </pre>
			 */
			public BridgeChatProtoBuf.BindingRequestOrBuilder getBindingRequestOrBuilder() {
				return bindingRequest_;
			}

			// optional .BindingResponse binding_response = 102;
			public static final int BINDING_RESPONSE_FIELD_NUMBER = 102;
			private BridgeChatProtoBuf.BindingResponse bindingResponse_;

			/**
			 * <code>optional .BindingResponse binding_response = 102;</code>
			 */
			public boolean hasBindingResponse() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			/**
			 * <code>optional .BindingResponse binding_response = 102;</code>
			 */
			public BridgeChatProtoBuf.BindingResponse getBindingResponse() {
				return bindingResponse_;
			}

			/**
			 * <code>optional .BindingResponse binding_response = 102;</code>
			 */
			public BridgeChatProtoBuf.BindingResponseOrBuilder getBindingResponseOrBuilder() {
				return bindingResponse_;
			}

			// optional .UnbindRequest unbind_request = 103;
			public static final int UNBIND_REQUEST_FIELD_NUMBER = 103;
			private BridgeChatProtoBuf.UnbindRequest unbindRequest_;

			/**
			 * <code>optional .UnbindRequest unbind_request = 103;</code>
			 */
			public boolean hasUnbindRequest() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			/**
			 * <code>optional .UnbindRequest unbind_request = 103;</code>
			 */
			public BridgeChatProtoBuf.UnbindRequest getUnbindRequest() {
				return unbindRequest_;
			}

			/**
			 * <code>optional .UnbindRequest unbind_request = 103;</code>
			 */
			public BridgeChatProtoBuf.UnbindRequestOrBuilder getUnbindRequestOrBuilder() {
				return unbindRequest_;
			}

			// optional .GroupStatus group_status_change = 104;
			public static final int GROUP_STATUS_CHANGE_FIELD_NUMBER = 104;
			private BridgeChatProtoBuf.GroupStatus groupStatusChange_;

			/**
			 * <code>optional .GroupStatus group_status_change = 104;</code>
			 */
			public boolean hasGroupStatusChange() {
				return ((bitField0_ & 0x00000010) == 0x00000010);
			}

			/**
			 * <code>optional .GroupStatus group_status_change = 104;</code>
			 */
			public BridgeChatProtoBuf.GroupStatus getGroupStatusChange() {
				return groupStatusChange_;
			}

			/**
			 * <code>optional .GroupStatus group_status_change = 104;</code>
			 */
			public BridgeChatProtoBuf.GroupStatusOrBuilder getGroupStatusChangeOrBuilder() {
				return groupStatusChange_;
			}

			// optional .UserEvent user_event = 105;
			public static final int USER_EVENT_FIELD_NUMBER = 105;
			private BridgeChatProtoBuf.UserEvent userEvent_;

			/**
			 * <code>optional .UserEvent user_event = 105;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			public boolean hasUserEvent() {
				return ((bitField0_ & 0x00000020) == 0x00000020);
			}

			/**
			 * <code>optional .UserEvent user_event = 105;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			public BridgeChatProtoBuf.UserEvent getUserEvent() {
				return userEvent_;
			}

			/**
			 * <code>optional .UserEvent user_event = 105;</code>
			 *
			 * <pre>
			 * }
			 * </pre>
			 */
			public BridgeChatProtoBuf.UserEventOrBuilder getUserEventOrBuilder() {
				return userEvent_;
			}

			private void initFields() {
				groupId_ = 0;
				bindingRequest_ = BridgeChatProtoBuf.BindingRequest
						.getDefaultInstance();
				bindingResponse_ = BridgeChatProtoBuf.BindingResponse
						.getDefaultInstance();
				unbindRequest_ = BridgeChatProtoBuf.UnbindRequest
						.getDefaultInstance();
				groupStatusChange_ = BridgeChatProtoBuf.GroupStatus
						.getDefaultInstance();
				userEvent_ = BridgeChatProtoBuf.UserEvent.getDefaultInstance();
			}

			private byte memoizedIsInitialized = -1;

			public final boolean isInitialized() {
				byte isInitialized = memoizedIsInitialized;
				if (isInitialized != -1)
					return isInitialized == 1;

				if (!hasGroupId()) {
					memoizedIsInitialized = 0;
					return false;
				}
				if (hasBindingRequest()) {
					if (!getBindingRequest().isInitialized()) {
						memoizedIsInitialized = 0;
						return false;
					}
				}
				if (hasBindingResponse()) {
					if (!getBindingResponse().isInitialized()) {
						memoizedIsInitialized = 0;
						return false;
					}
				}
				if (hasUserEvent()) {
					if (!getUserEvent().isInitialized()) {
						memoizedIsInitialized = 0;
						return false;
					}
				}
				memoizedIsInitialized = 1;
				return true;
			}

			public void writeTo(com.google.protobuf.CodedOutputStream output)
					throws java.io.IOException {
				getSerializedSize();
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					output.writeUInt32(1, groupId_);
				}
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					output.writeMessage(101, bindingRequest_);
				}
				if (((bitField0_ & 0x00000004) == 0x00000004)) {
					output.writeMessage(102, bindingResponse_);
				}
				if (((bitField0_ & 0x00000008) == 0x00000008)) {
					output.writeMessage(103, unbindRequest_);
				}
				if (((bitField0_ & 0x00000010) == 0x00000010)) {
					output.writeMessage(104, groupStatusChange_);
				}
				if (((bitField0_ & 0x00000020) == 0x00000020)) {
					output.writeMessage(105, userEvent_);
				}
				getUnknownFields().writeTo(output);
			}

			private int memoizedSerializedSize = -1;

			public int getSerializedSize() {
				int size = memoizedSerializedSize;
				if (size != -1)
					return size;

				size = 0;
				if (((bitField0_ & 0x00000001) == 0x00000001)) {
					size += com.google.protobuf.CodedOutputStream
							.computeUInt32Size(1, groupId_);
				}
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					size += com.google.protobuf.CodedOutputStream
							.computeMessageSize(101, bindingRequest_);
				}
				if (((bitField0_ & 0x00000004) == 0x00000004)) {
					size += com.google.protobuf.CodedOutputStream
							.computeMessageSize(102, bindingResponse_);
				}
				if (((bitField0_ & 0x00000008) == 0x00000008)) {
					size += com.google.protobuf.CodedOutputStream
							.computeMessageSize(103, unbindRequest_);
				}
				if (((bitField0_ & 0x00000010) == 0x00000010)) {
					size += com.google.protobuf.CodedOutputStream
							.computeMessageSize(104, groupStatusChange_);
				}
				if (((bitField0_ & 0x00000020) == 0x00000020)) {
					size += com.google.protobuf.CodedOutputStream
							.computeMessageSize(105, userEvent_);
				}
				size += getUnknownFields().getSerializedSize();
				memoizedSerializedSize = size;
				return size;
			}

			private static final long serialVersionUID = 0L;

			@java.lang.Override
			protected java.lang.Object writeReplace()
					throws java.io.ObjectStreamException {
				return super.writeReplace();
			}

			public static BridgeChatProtoBuf.GroupMessage parseFrom(
					com.google.protobuf.ByteString data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.GroupMessage parseFrom(
					com.google.protobuf.ByteString data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.GroupMessage parseFrom(byte[] data)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data);
			}

			public static BridgeChatProtoBuf.GroupMessage parseFrom(
					byte[] data,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return PARSER.parseFrom(data, extensionRegistry);
			}

			public static BridgeChatProtoBuf.GroupMessage parseFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.GroupMessage parseFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.GroupMessage parseDelimitedFrom(
					java.io.InputStream input) throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input);
			}

			public static BridgeChatProtoBuf.GroupMessage parseDelimitedFrom(
					java.io.InputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseDelimitedFrom(input, extensionRegistry);
			}

			public static BridgeChatProtoBuf.GroupMessage parseFrom(
					com.google.protobuf.CodedInputStream input)
					throws java.io.IOException {
				return PARSER.parseFrom(input);
			}

			public static BridgeChatProtoBuf.GroupMessage parseFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException {
				return PARSER.parseFrom(input, extensionRegistry);
			}

			public static Builder newBuilder() {
				return Builder.create();
			}

			public Builder newBuilderForType() {
				return newBuilder();
			}

			public static Builder newBuilder(
					BridgeChatProtoBuf.GroupMessage prototype) {
				return newBuilder().mergeFrom(prototype);
			}

			public Builder toBuilder() {
				return newBuilder(this);
			}

			@java.lang.Override
			protected Builder newBuilderForType(
					com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				Builder builder = new Builder(parent);
				return builder;
			}

			/**
			 * Protobuf type {@code GroupMessage}
			 */
			public static final class Builder extends
					com.google.protobuf.GeneratedMessage.Builder<Builder>
					implements BridgeChatProtoBuf.GroupMessageOrBuilder {
				public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
					return BridgeChatProtoBuf.internal_static_GroupMessage_descriptor;
				}

				protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
					return BridgeChatProtoBuf.internal_static_GroupMessage_fieldAccessorTable
							.ensureFieldAccessorsInitialized(
									BridgeChatProtoBuf.GroupMessage.class,
									BridgeChatProtoBuf.GroupMessage.Builder.class);
				}

				// Construct using BridgeChat.GroupMessage.newBuilder()
				private Builder() {
					maybeForceBuilderInitialization();
				}

				private Builder(
						com.google.protobuf.GeneratedMessage.BuilderParent parent) {
					super(parent);
					maybeForceBuilderInitialization();
				}

				private void maybeForceBuilderInitialization() {
					if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
						getBindingRequestFieldBuilder();
						getBindingResponseFieldBuilder();
						getUnbindRequestFieldBuilder();
						getGroupStatusChangeFieldBuilder();
						getUserEventFieldBuilder();
					}
				}

				private static Builder create() {
					return new Builder();
				}

				public Builder clear() {
					super.clear();
					groupId_ = 0;
					bitField0_ = (bitField0_ & ~0x00000001);
					if (bindingRequestBuilder_ == null) {
						bindingRequest_ = BridgeChatProtoBuf.BindingRequest
								.getDefaultInstance();
					} else {
						bindingRequestBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000002);
					if (bindingResponseBuilder_ == null) {
						bindingResponse_ = BridgeChatProtoBuf.BindingResponse
								.getDefaultInstance();
					} else {
						bindingResponseBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000004);
					if (unbindRequestBuilder_ == null) {
						unbindRequest_ = BridgeChatProtoBuf.UnbindRequest
								.getDefaultInstance();
					} else {
						unbindRequestBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000008);
					if (groupStatusChangeBuilder_ == null) {
						groupStatusChange_ = BridgeChatProtoBuf.GroupStatus
								.getDefaultInstance();
					} else {
						groupStatusChangeBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000010);
					if (userEventBuilder_ == null) {
						userEvent_ = BridgeChatProtoBuf.UserEvent
								.getDefaultInstance();
					} else {
						userEventBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000020);
					return this;
				}

				public Builder clone() {
					return create().mergeFrom(buildPartial());
				}

				public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
					return BridgeChatProtoBuf.internal_static_GroupMessage_descriptor;
				}

				public BridgeChatProtoBuf.GroupMessage getDefaultInstanceForType() {
					return BridgeChatProtoBuf.GroupMessage.getDefaultInstance();
				}

				public BridgeChatProtoBuf.GroupMessage build() {
					BridgeChatProtoBuf.GroupMessage result = buildPartial();
					if (!result.isInitialized()) {
						throw newUninitializedMessageException(result);
					}
					return result;
				}

				public BridgeChatProtoBuf.GroupMessage buildPartial() {
					BridgeChatProtoBuf.GroupMessage result = new BridgeChatProtoBuf.GroupMessage(
							this);
					int from_bitField0_ = bitField0_;
					int to_bitField0_ = 0;
					if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
						to_bitField0_ |= 0x00000001;
					}
					result.groupId_ = groupId_;
					if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
						to_bitField0_ |= 0x00000002;
					}
					if (bindingRequestBuilder_ == null) {
						result.bindingRequest_ = bindingRequest_;
					} else {
						result.bindingRequest_ = bindingRequestBuilder_.build();
					}
					if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
						to_bitField0_ |= 0x00000004;
					}
					if (bindingResponseBuilder_ == null) {
						result.bindingResponse_ = bindingResponse_;
					} else {
						result.bindingResponse_ = bindingResponseBuilder_
								.build();
					}
					if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
						to_bitField0_ |= 0x00000008;
					}
					if (unbindRequestBuilder_ == null) {
						result.unbindRequest_ = unbindRequest_;
					} else {
						result.unbindRequest_ = unbindRequestBuilder_.build();
					}
					if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
						to_bitField0_ |= 0x00000010;
					}
					if (groupStatusChangeBuilder_ == null) {
						result.groupStatusChange_ = groupStatusChange_;
					} else {
						result.groupStatusChange_ = groupStatusChangeBuilder_
								.build();
					}
					if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
						to_bitField0_ |= 0x00000020;
					}
					if (userEventBuilder_ == null) {
						result.userEvent_ = userEvent_;
					} else {
						result.userEvent_ = userEventBuilder_.build();
					}
					result.bitField0_ = to_bitField0_;
					onBuilt();
					return result;
				}

				public Builder mergeFrom(com.google.protobuf.Message other) {
					if (other instanceof BridgeChatProtoBuf.GroupMessage) {
						return mergeFrom((BridgeChatProtoBuf.GroupMessage) other);
					} else {
						super.mergeFrom(other);
						return this;
					}
				}

				public Builder mergeFrom(BridgeChatProtoBuf.GroupMessage other) {
					if (other == BridgeChatProtoBuf.GroupMessage
							.getDefaultInstance())
						return this;
					if (other.hasGroupId()) {
						setGroupId(other.getGroupId());
					}
					if (other.hasBindingRequest()) {
						mergeBindingRequest(other.getBindingRequest());
					}
					if (other.hasBindingResponse()) {
						mergeBindingResponse(other.getBindingResponse());
					}
					if (other.hasUnbindRequest()) {
						mergeUnbindRequest(other.getUnbindRequest());
					}
					if (other.hasGroupStatusChange()) {
						mergeGroupStatusChange(other.getGroupStatusChange());
					}
					if (other.hasUserEvent()) {
						mergeUserEvent(other.getUserEvent());
					}
					this.mergeUnknownFields(other.getUnknownFields());
					return this;
				}

				public final boolean isInitialized() {
					if (!hasGroupId()) {

						return false;
					}
					if (hasBindingRequest()) {
						if (!getBindingRequest().isInitialized()) {

							return false;
						}
					}
					if (hasBindingResponse()) {
						if (!getBindingResponse().isInitialized()) {

							return false;
						}
					}
					if (hasUserEvent()) {
						if (!getUserEvent().isInitialized()) {

							return false;
						}
					}
					return true;
				}

				public Builder mergeFrom(
						com.google.protobuf.CodedInputStream input,
						com.google.protobuf.ExtensionRegistryLite extensionRegistry)
						throws java.io.IOException {
					BridgeChatProtoBuf.GroupMessage parsedMessage = null;
					try {
						parsedMessage = PARSER.parsePartialFrom(input,
								extensionRegistry);
					} catch (com.google.protobuf.InvalidProtocolBufferException e) {
						parsedMessage = (BridgeChatProtoBuf.GroupMessage) e
								.getUnfinishedMessage();
						throw e;
					} finally {
						if (parsedMessage != null) {
							mergeFrom(parsedMessage);
						}
					}
					return this;
				}

				private int bitField0_;

				// required uint32 group_id = 1;
				private int groupId_;

				/**
				 * <code>required uint32 group_id = 1;</code>
				 */
				public boolean hasGroupId() {
					return ((bitField0_ & 0x00000001) == 0x00000001);
				}

				/**
				 * <code>required uint32 group_id = 1;</code>
				 */
				public int getGroupId() {
					return groupId_;
				}

				/**
				 * <code>required uint32 group_id = 1;</code>
				 */
				public Builder setGroupId(int value) {
					bitField0_ |= 0x00000001;
					groupId_ = value;
					onChanged();
					return this;
				}

				/**
				 * <code>required uint32 group_id = 1;</code>
				 */
				public Builder clearGroupId() {
					bitField0_ = (bitField0_ & ~0x00000001);
					groupId_ = 0;
					onChanged();
					return this;
				}

				// optional .BindingRequest binding_request = 101;
				private BridgeChatProtoBuf.BindingRequest bindingRequest_ = BridgeChatProtoBuf.BindingRequest
						.getDefaultInstance();
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.BindingRequest, BridgeChatProtoBuf.BindingRequest.Builder, BridgeChatProtoBuf.BindingRequestOrBuilder> bindingRequestBuilder_;

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public boolean hasBindingRequest() {
					return ((bitField0_ & 0x00000002) == 0x00000002);
				}

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public BridgeChatProtoBuf.BindingRequest getBindingRequest() {
					if (bindingRequestBuilder_ == null) {
						return bindingRequest_;
					} else {
						return bindingRequestBuilder_.getMessage();
					}
				}

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public Builder setBindingRequest(
						BridgeChatProtoBuf.BindingRequest value) {
					if (bindingRequestBuilder_ == null) {
						if (value == null) {
							throw new NullPointerException();
						}
						bindingRequest_ = value;
						onChanged();
					} else {
						bindingRequestBuilder_.setMessage(value);
					}
					bitField0_ |= 0x00000002;
					return this;
				}

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public Builder setBindingRequest(
						BridgeChatProtoBuf.BindingRequest.Builder builderForValue) {
					if (bindingRequestBuilder_ == null) {
						bindingRequest_ = builderForValue.build();
						onChanged();
					} else {
						bindingRequestBuilder_.setMessage(builderForValue
								.build());
					}
					bitField0_ |= 0x00000002;
					return this;
				}

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public Builder mergeBindingRequest(
						BridgeChatProtoBuf.BindingRequest value) {
					if (bindingRequestBuilder_ == null) {
						if (((bitField0_ & 0x00000002) == 0x00000002)
								&& bindingRequest_ != BridgeChatProtoBuf.BindingRequest
										.getDefaultInstance()) {
							bindingRequest_ = BridgeChatProtoBuf.BindingRequest
									.newBuilder(bindingRequest_)
									.mergeFrom(value).buildPartial();
						} else {
							bindingRequest_ = value;
						}
						onChanged();
					} else {
						bindingRequestBuilder_.mergeFrom(value);
					}
					bitField0_ |= 0x00000002;
					return this;
				}

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public Builder clearBindingRequest() {
					if (bindingRequestBuilder_ == null) {
						bindingRequest_ = BridgeChatProtoBuf.BindingRequest
								.getDefaultInstance();
						onChanged();
					} else {
						bindingRequestBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000002);
					return this;
				}

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public BridgeChatProtoBuf.BindingRequest.Builder getBindingRequestBuilder() {
					bitField0_ |= 0x00000002;
					onChanged();
					return getBindingRequestFieldBuilder().getBuilder();
				}

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				public BridgeChatProtoBuf.BindingRequestOrBuilder getBindingRequestOrBuilder() {
					if (bindingRequestBuilder_ != null) {
						return bindingRequestBuilder_.getMessageOrBuilder();
					} else {
						return bindingRequest_;
					}
				}

				/**
				 * <code>optional .BindingRequest binding_request = 101;</code>
				 *
				 * <pre>
				 * oneof {
				 * </pre>
				 */
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.BindingRequest, BridgeChatProtoBuf.BindingRequest.Builder, BridgeChatProtoBuf.BindingRequestOrBuilder> getBindingRequestFieldBuilder() {
					if (bindingRequestBuilder_ == null) {
						bindingRequestBuilder_ = new com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.BindingRequest, BridgeChatProtoBuf.BindingRequest.Builder, BridgeChatProtoBuf.BindingRequestOrBuilder>(
								bindingRequest_, getParentForChildren(),
								isClean());
						bindingRequest_ = null;
					}
					return bindingRequestBuilder_;
				}

				// optional .BindingResponse binding_response = 102;
				private BridgeChatProtoBuf.BindingResponse bindingResponse_ = BridgeChatProtoBuf.BindingResponse
						.getDefaultInstance();
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.BindingResponse, BridgeChatProtoBuf.BindingResponse.Builder, BridgeChatProtoBuf.BindingResponseOrBuilder> bindingResponseBuilder_;

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				public boolean hasBindingResponse() {
					return ((bitField0_ & 0x00000004) == 0x00000004);
				}

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				public BridgeChatProtoBuf.BindingResponse getBindingResponse() {
					if (bindingResponseBuilder_ == null) {
						return bindingResponse_;
					} else {
						return bindingResponseBuilder_.getMessage();
					}
				}

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				public Builder setBindingResponse(
						BridgeChatProtoBuf.BindingResponse value) {
					if (bindingResponseBuilder_ == null) {
						if (value == null) {
							throw new NullPointerException();
						}
						bindingResponse_ = value;
						onChanged();
					} else {
						bindingResponseBuilder_.setMessage(value);
					}
					bitField0_ |= 0x00000004;
					return this;
				}

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				public Builder setBindingResponse(
						BridgeChatProtoBuf.BindingResponse.Builder builderForValue) {
					if (bindingResponseBuilder_ == null) {
						bindingResponse_ = builderForValue.build();
						onChanged();
					} else {
						bindingResponseBuilder_.setMessage(builderForValue
								.build());
					}
					bitField0_ |= 0x00000004;
					return this;
				}

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				public Builder mergeBindingResponse(
						BridgeChatProtoBuf.BindingResponse value) {
					if (bindingResponseBuilder_ == null) {
						if (((bitField0_ & 0x00000004) == 0x00000004)
								&& bindingResponse_ != BridgeChatProtoBuf.BindingResponse
										.getDefaultInstance()) {
							bindingResponse_ = BridgeChatProtoBuf.BindingResponse
									.newBuilder(bindingResponse_)
									.mergeFrom(value).buildPartial();
						} else {
							bindingResponse_ = value;
						}
						onChanged();
					} else {
						bindingResponseBuilder_.mergeFrom(value);
					}
					bitField0_ |= 0x00000004;
					return this;
				}

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				public Builder clearBindingResponse() {
					if (bindingResponseBuilder_ == null) {
						bindingResponse_ = BridgeChatProtoBuf.BindingResponse
								.getDefaultInstance();
						onChanged();
					} else {
						bindingResponseBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000004);
					return this;
				}

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				public BridgeChatProtoBuf.BindingResponse.Builder getBindingResponseBuilder() {
					bitField0_ |= 0x00000004;
					onChanged();
					return getBindingResponseFieldBuilder().getBuilder();
				}

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				public BridgeChatProtoBuf.BindingResponseOrBuilder getBindingResponseOrBuilder() {
					if (bindingResponseBuilder_ != null) {
						return bindingResponseBuilder_.getMessageOrBuilder();
					} else {
						return bindingResponse_;
					}
				}

				/**
				 * <code>optional .BindingResponse binding_response = 102;</code>
				 */
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.BindingResponse, BridgeChatProtoBuf.BindingResponse.Builder, BridgeChatProtoBuf.BindingResponseOrBuilder> getBindingResponseFieldBuilder() {
					if (bindingResponseBuilder_ == null) {
						bindingResponseBuilder_ = new com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.BindingResponse, BridgeChatProtoBuf.BindingResponse.Builder, BridgeChatProtoBuf.BindingResponseOrBuilder>(
								bindingResponse_, getParentForChildren(),
								isClean());
						bindingResponse_ = null;
					}
					return bindingResponseBuilder_;
				}

				// optional .UnbindRequest unbind_request = 103;
				private BridgeChatProtoBuf.UnbindRequest unbindRequest_ = BridgeChatProtoBuf.UnbindRequest
						.getDefaultInstance();
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UnbindRequest, BridgeChatProtoBuf.UnbindRequest.Builder, BridgeChatProtoBuf.UnbindRequestOrBuilder> unbindRequestBuilder_;

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				public boolean hasUnbindRequest() {
					return ((bitField0_ & 0x00000008) == 0x00000008);
				}

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				public BridgeChatProtoBuf.UnbindRequest getUnbindRequest() {
					if (unbindRequestBuilder_ == null) {
						return unbindRequest_;
					} else {
						return unbindRequestBuilder_.getMessage();
					}
				}

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				public Builder setUnbindRequest(
						BridgeChatProtoBuf.UnbindRequest value) {
					if (unbindRequestBuilder_ == null) {
						if (value == null) {
							throw new NullPointerException();
						}
						unbindRequest_ = value;
						onChanged();
					} else {
						unbindRequestBuilder_.setMessage(value);
					}
					bitField0_ |= 0x00000008;
					return this;
				}

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				public Builder setUnbindRequest(
						BridgeChatProtoBuf.UnbindRequest.Builder builderForValue) {
					if (unbindRequestBuilder_ == null) {
						unbindRequest_ = builderForValue.build();
						onChanged();
					} else {
						unbindRequestBuilder_.setMessage(builderForValue
								.build());
					}
					bitField0_ |= 0x00000008;
					return this;
				}

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				public Builder mergeUnbindRequest(
						BridgeChatProtoBuf.UnbindRequest value) {
					if (unbindRequestBuilder_ == null) {
						if (((bitField0_ & 0x00000008) == 0x00000008)
								&& unbindRequest_ != BridgeChatProtoBuf.UnbindRequest
										.getDefaultInstance()) {
							unbindRequest_ = BridgeChatProtoBuf.UnbindRequest
									.newBuilder(unbindRequest_)
									.mergeFrom(value).buildPartial();
						} else {
							unbindRequest_ = value;
						}
						onChanged();
					} else {
						unbindRequestBuilder_.mergeFrom(value);
					}
					bitField0_ |= 0x00000008;
					return this;
				}

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				public Builder clearUnbindRequest() {
					if (unbindRequestBuilder_ == null) {
						unbindRequest_ = BridgeChatProtoBuf.UnbindRequest
								.getDefaultInstance();
						onChanged();
					} else {
						unbindRequestBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000008);
					return this;
				}

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				public BridgeChatProtoBuf.UnbindRequest.Builder getUnbindRequestBuilder() {
					bitField0_ |= 0x00000008;
					onChanged();
					return getUnbindRequestFieldBuilder().getBuilder();
				}

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				public BridgeChatProtoBuf.UnbindRequestOrBuilder getUnbindRequestOrBuilder() {
					if (unbindRequestBuilder_ != null) {
						return unbindRequestBuilder_.getMessageOrBuilder();
					} else {
						return unbindRequest_;
					}
				}

				/**
				 * <code>optional .UnbindRequest unbind_request = 103;</code>
				 */
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UnbindRequest, BridgeChatProtoBuf.UnbindRequest.Builder, BridgeChatProtoBuf.UnbindRequestOrBuilder> getUnbindRequestFieldBuilder() {
					if (unbindRequestBuilder_ == null) {
						unbindRequestBuilder_ = new com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UnbindRequest, BridgeChatProtoBuf.UnbindRequest.Builder, BridgeChatProtoBuf.UnbindRequestOrBuilder>(
								unbindRequest_, getParentForChildren(),
								isClean());
						unbindRequest_ = null;
					}
					return unbindRequestBuilder_;
				}

				// optional .GroupStatus group_status_change = 104;
				private BridgeChatProtoBuf.GroupStatus groupStatusChange_ = BridgeChatProtoBuf.GroupStatus
						.getDefaultInstance();
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.GroupStatus, BridgeChatProtoBuf.GroupStatus.Builder, BridgeChatProtoBuf.GroupStatusOrBuilder> groupStatusChangeBuilder_;

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				public boolean hasGroupStatusChange() {
					return ((bitField0_ & 0x00000010) == 0x00000010);
				}

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				public BridgeChatProtoBuf.GroupStatus getGroupStatusChange() {
					if (groupStatusChangeBuilder_ == null) {
						return groupStatusChange_;
					} else {
						return groupStatusChangeBuilder_.getMessage();
					}
				}

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				public Builder setGroupStatusChange(
						BridgeChatProtoBuf.GroupStatus value) {
					if (groupStatusChangeBuilder_ == null) {
						if (value == null) {
							throw new NullPointerException();
						}
						groupStatusChange_ = value;
						onChanged();
					} else {
						groupStatusChangeBuilder_.setMessage(value);
					}
					bitField0_ |= 0x00000010;
					return this;
				}

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				public Builder setGroupStatusChange(
						BridgeChatProtoBuf.GroupStatus.Builder builderForValue) {
					if (groupStatusChangeBuilder_ == null) {
						groupStatusChange_ = builderForValue.build();
						onChanged();
					} else {
						groupStatusChangeBuilder_.setMessage(builderForValue
								.build());
					}
					bitField0_ |= 0x00000010;
					return this;
				}

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				public Builder mergeGroupStatusChange(
						BridgeChatProtoBuf.GroupStatus value) {
					if (groupStatusChangeBuilder_ == null) {
						if (((bitField0_ & 0x00000010) == 0x00000010)
								&& groupStatusChange_ != BridgeChatProtoBuf.GroupStatus
										.getDefaultInstance()) {
							groupStatusChange_ = BridgeChatProtoBuf.GroupStatus
									.newBuilder(groupStatusChange_)
									.mergeFrom(value).buildPartial();
						} else {
							groupStatusChange_ = value;
						}
						onChanged();
					} else {
						groupStatusChangeBuilder_.mergeFrom(value);
					}
					bitField0_ |= 0x00000010;
					return this;
				}

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				public Builder clearGroupStatusChange() {
					if (groupStatusChangeBuilder_ == null) {
						groupStatusChange_ = BridgeChatProtoBuf.GroupStatus
								.getDefaultInstance();
						onChanged();
					} else {
						groupStatusChangeBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000010);
					return this;
				}

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				public BridgeChatProtoBuf.GroupStatus.Builder getGroupStatusChangeBuilder() {
					bitField0_ |= 0x00000010;
					onChanged();
					return getGroupStatusChangeFieldBuilder().getBuilder();
				}

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				public BridgeChatProtoBuf.GroupStatusOrBuilder getGroupStatusChangeOrBuilder() {
					if (groupStatusChangeBuilder_ != null) {
						return groupStatusChangeBuilder_.getMessageOrBuilder();
					} else {
						return groupStatusChange_;
					}
				}

				/**
				 * <code>optional .GroupStatus group_status_change = 104;</code>
				 */
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.GroupStatus, BridgeChatProtoBuf.GroupStatus.Builder, BridgeChatProtoBuf.GroupStatusOrBuilder> getGroupStatusChangeFieldBuilder() {
					if (groupStatusChangeBuilder_ == null) {
						groupStatusChangeBuilder_ = new com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.GroupStatus, BridgeChatProtoBuf.GroupStatus.Builder, BridgeChatProtoBuf.GroupStatusOrBuilder>(
								groupStatusChange_, getParentForChildren(),
								isClean());
						groupStatusChange_ = null;
					}
					return groupStatusChangeBuilder_;
				}

				// optional .UserEvent user_event = 105;
				private BridgeChatProtoBuf.UserEvent userEvent_ = BridgeChatProtoBuf.UserEvent
						.getDefaultInstance();
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UserEvent, BridgeChatProtoBuf.UserEvent.Builder, BridgeChatProtoBuf.UserEventOrBuilder> userEventBuilder_;

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public boolean hasUserEvent() {
					return ((bitField0_ & 0x00000020) == 0x00000020);
				}

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public BridgeChatProtoBuf.UserEvent getUserEvent() {
					if (userEventBuilder_ == null) {
						return userEvent_;
					} else {
						return userEventBuilder_.getMessage();
					}
				}

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public Builder setUserEvent(BridgeChatProtoBuf.UserEvent value) {
					if (userEventBuilder_ == null) {
						if (value == null) {
							throw new NullPointerException();
						}
						userEvent_ = value;
						onChanged();
					} else {
						userEventBuilder_.setMessage(value);
					}
					bitField0_ |= 0x00000020;
					return this;
				}

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public Builder setUserEvent(
						BridgeChatProtoBuf.UserEvent.Builder builderForValue) {
					if (userEventBuilder_ == null) {
						userEvent_ = builderForValue.build();
						onChanged();
					} else {
						userEventBuilder_.setMessage(builderForValue.build());
					}
					bitField0_ |= 0x00000020;
					return this;
				}

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public Builder mergeUserEvent(BridgeChatProtoBuf.UserEvent value) {
					if (userEventBuilder_ == null) {
						if (((bitField0_ & 0x00000020) == 0x00000020)
								&& userEvent_ != BridgeChatProtoBuf.UserEvent
										.getDefaultInstance()) {
							userEvent_ = BridgeChatProtoBuf.UserEvent
									.newBuilder(userEvent_).mergeFrom(value)
									.buildPartial();
						} else {
							userEvent_ = value;
						}
						onChanged();
					} else {
						userEventBuilder_.mergeFrom(value);
					}
					bitField0_ |= 0x00000020;
					return this;
				}

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public Builder clearUserEvent() {
					if (userEventBuilder_ == null) {
						userEvent_ = BridgeChatProtoBuf.UserEvent
								.getDefaultInstance();
						onChanged();
					} else {
						userEventBuilder_.clear();
					}
					bitField0_ = (bitField0_ & ~0x00000020);
					return this;
				}

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public BridgeChatProtoBuf.UserEvent.Builder getUserEventBuilder() {
					bitField0_ |= 0x00000020;
					onChanged();
					return getUserEventFieldBuilder().getBuilder();
				}

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				public BridgeChatProtoBuf.UserEventOrBuilder getUserEventOrBuilder() {
					if (userEventBuilder_ != null) {
						return userEventBuilder_.getMessageOrBuilder();
					} else {
						return userEvent_;
					}
				}

				/**
				 * <code>optional .UserEvent user_event = 105;</code>
				 *
				 * <pre>
				 * }
				 * </pre>
				 */
				private com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UserEvent, BridgeChatProtoBuf.UserEvent.Builder, BridgeChatProtoBuf.UserEventOrBuilder> getUserEventFieldBuilder() {
					if (userEventBuilder_ == null) {
						userEventBuilder_ = new com.google.protobuf.SingleFieldBuilder<BridgeChatProtoBuf.UserEvent, BridgeChatProtoBuf.UserEvent.Builder, BridgeChatProtoBuf.UserEventOrBuilder>(
								userEvent_, getParentForChildren(), isClean());
						userEvent_ = null;
					}
					return userEventBuilder_;
				}

				// @@protoc_insertion_point(builder_scope:GroupMessage)
			}

			static {
				defaultInstance = new GroupMessage(true);
				defaultInstance.initFields();
			}

			// @@protoc_insertion_point(class_scope:GroupMessage)
		}

		private static com.google.protobuf.Descriptors.Descriptor internal_static_ModuleIntro_descriptor;
		private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_ModuleIntro_fieldAccessorTable;
		private static com.google.protobuf.Descriptors.Descriptor internal_static_BindingRequest_descriptor;
		private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_BindingRequest_fieldAccessorTable;
		private static com.google.protobuf.Descriptors.Descriptor internal_static_BindingResponse_descriptor;
		private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_BindingResponse_fieldAccessorTable;
		private static com.google.protobuf.Descriptors.Descriptor internal_static_UnbindRequest_descriptor;
		private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_UnbindRequest_fieldAccessorTable;
		private static com.google.protobuf.Descriptors.Descriptor internal_static_UserStatus_descriptor;
		private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_UserStatus_fieldAccessorTable;
		private static com.google.protobuf.Descriptors.Descriptor internal_static_UserEvent_descriptor;
		private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_UserEvent_fieldAccessorTable;
		private static com.google.protobuf.Descriptors.Descriptor internal_static_GroupStatus_descriptor;
		private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_GroupStatus_fieldAccessorTable;
		private static com.google.protobuf.Descriptors.Descriptor internal_static_GroupMessage_descriptor;
		private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_GroupMessage_fieldAccessorTable;

		public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
			return descriptor;
		}

		private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
		static {
			java.lang.String[] descriptorData = {
					"\n\020BridgeChat.proto\"4\n\013ModuleIntro\022\021\n\tlon"
							+ "g_name\030\001 \002(\t\022\022\n\nshort_name\030\002 \002(\t\"#\n\016Bind"
							+ "ingRequest\022\021\n\tbind_info\030\001 \002(\t\"6\n\017Binding"
							+ "Response\022\017\n\007success\030\001 \002(\010\022\022\n\ndiagnostic\030"
							+ "\002 \001(\t\"\017\n\rUnbindRequest\"#\n\nUserStatus\022\025\n\r"
							+ "online_status\030\002 \001(\010\"h\n\tUserEvent\022\020\n\010user"
							+ "name\030\001 \002(\t\022\021\n\tplugin_id\030\002 \001(\t\022 \n\013user_st"
							+ "atus\030e \001(\0132\013.UserStatus\022\024\n\014chat_message\030"
							+ "f \001(\t\"\034\n\013GroupStatus\022\r\n\005topic\030\001 \001(\t\"\351\001\n\014"
							+ "GroupMessage\022\020\n\010group_id\030\001 \002(\r\022(\n\017bindin",
					"g_request\030e \001(\0132\017.BindingRequest\022*\n\020bind"
							+ "ing_response\030f \001(\0132\020.BindingResponse\022&\n\016"
							+ "unbind_request\030g \001(\0132\016.UnbindRequest\022)\n\023"
							+ "group_status_change\030h \001(\0132\014.GroupStatus\022"
							+ "\036\n\nuser_event\030i \001(\0132\n.UserEvent" };
			com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
				public com.google.protobuf.ExtensionRegistry assignDescriptors(
						com.google.protobuf.Descriptors.FileDescriptor root) {
					descriptor = root;
					internal_static_ModuleIntro_descriptor = getDescriptor()
							.getMessageTypes().get(0);
					internal_static_ModuleIntro_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
							internal_static_ModuleIntro_descriptor,
							new java.lang.String[] { "LongName", "ShortName", });
					internal_static_BindingRequest_descriptor = getDescriptor()
							.getMessageTypes().get(1);
					internal_static_BindingRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
							internal_static_BindingRequest_descriptor,
							new java.lang.String[] { "BindInfo", });
					internal_static_BindingResponse_descriptor = getDescriptor()
							.getMessageTypes().get(2);
					internal_static_BindingResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
							internal_static_BindingResponse_descriptor,
							new java.lang.String[] { "Success", "Diagnostic", });
					internal_static_UnbindRequest_descriptor = getDescriptor()
							.getMessageTypes().get(3);
					internal_static_UnbindRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
							internal_static_UnbindRequest_descriptor,
							new java.lang.String[] {});
					internal_static_UserStatus_descriptor = getDescriptor()
							.getMessageTypes().get(4);
					internal_static_UserStatus_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
							internal_static_UserStatus_descriptor,
							new java.lang.String[] { "OnlineStatus", });
					internal_static_UserEvent_descriptor = getDescriptor()
							.getMessageTypes().get(5);
					internal_static_UserEvent_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
							internal_static_UserEvent_descriptor,
							new java.lang.String[] { "Username", "PluginId",
									"UserStatus", "ChatMessage", });
					internal_static_GroupStatus_descriptor = getDescriptor()
							.getMessageTypes().get(6);
					internal_static_GroupStatus_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
							internal_static_GroupStatus_descriptor,
							new java.lang.String[] { "Topic", });
					internal_static_GroupMessage_descriptor = getDescriptor()
							.getMessageTypes().get(7);
					internal_static_GroupMessage_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
							internal_static_GroupMessage_descriptor,
							new java.lang.String[] { "GroupId",
									"BindingRequest", "BindingResponse",
									"UnbindRequest", "GroupStatusChange",
									"UserEvent", });
					return null;
				}
			};
			com.google.protobuf.Descriptors.FileDescriptor
					.internalBuildGeneratedFileFrom(
							descriptorData,
							new com.google.protobuf.Descriptors.FileDescriptor[] {},
							assigner);
		}

		// @@protoc_insertion_point(outer_class_scope)
	}

}
