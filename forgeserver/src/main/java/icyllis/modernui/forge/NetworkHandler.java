/*
 * Modern UI.
 * Copyright (C) 2019-2021 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * This class maintains a channel to {@link NetworkRegistry} that fixes some bugs.
 */
@SuppressWarnings("unused")
public class NetworkHandler {

    protected final ResourceLocation mName;

    protected final String mProtocol;
    protected final boolean mOptional;

    /**
     * Create a network handler of a mod. Note that this is a distribution-sensitive operation,
     * you must be careful with the class loading.
     *
     * @param name     the channel name
     * @param protocol the network protocol
     * @param optional allow absent or request same protocol?
     */
    public NetworkHandler(@Nonnull ResourceLocation name,
                          @Nonnull String protocol,
                          boolean optional) {
        mName = name;
        mProtocol = protocol;
        mOptional = optional;

        EventNetworkChannel channel = NetworkRegistry.newEventChannel(
                name,
                this::getProtocol,
                this::tryServerVersionOnClient,
                this::tryClientVersionOnServer);

        channel.addListener(this::onServerCustomPayload);
    }

    /**
     * Get the protocol string of this channel on current side.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return mProtocol;
    }

    /**
     * This method will run on client to verify the server protocol that sent by handshake network channel.
     *
     * @param protocol the protocol of this channel sent from server side
     * @return {@code true} to accept the protocol, {@code false} otherwise
     */
    protected boolean tryServerVersionOnClient(@Nonnull String protocol) {
        return mOptional && protocol.equals(NetworkRegistry.ABSENT) || mProtocol.equals(protocol);
    }

    /**
     * This method will run on server to verify the remote client protocol that sent by handshake network channel.
     *
     * @param protocol the protocol of this channel sent from client side
     * @return {@code true} to accept the protocol, {@code false} otherwise
     */
    protected boolean tryClientVersionOnServer(@Nonnull String protocol) {
        return mOptional && protocol.equals(NetworkRegistry.ABSENT) || mProtocol.equals(protocol);
    }

    protected void onServerCustomPayload(@Nonnull NetworkEvent.ClientCustomPayloadEvent event) {
        FriendlyByteBuf payload = event.getPayload();
        ServerPlayer currentPlayer = event.getSource().get().getSender();
        if (payload != null && event.getLoginIndex() == Integer.MAX_VALUE && currentPlayer != null) {
            handleServerMessage(payload.readUnsignedShort(), payload, event.getSource(), currentPlayer.server);
        }
        event.getSource().get().setPacketHandled(true);
    }

    /**
     * Callback for handling a client-to-server network message.
     * <p>
     * This method is invoked on the Netty-IO thread, you need to consume the payload
     * and then process it further through thread scheduling. In addition to consuming,
     * you can also retain the payload to prevent it from being released after this
     * method call. In the latter case, you must manually release the payload.
     * <p>
     * Note that you should use {@link #getServerPlayer(Supplier)} to get the player.
     * <p>
     * You should do safety check with player before making changes to the game world.
     * Any player who can join the server may hack the protocol to send packets.
     * Do not trust any player UUID that requests permissions in the packet payload.
     *
     * @param index   the message index
     * @param payload the message body
     * @param source  the network event source
     * @param looper  the game event loop
     * @see #getServerPlayer(Supplier)
     */
    protected void handleServerMessage(int index,
                                       @Nonnull FriendlyByteBuf payload,
                                       @Nonnull Supplier<NetworkEvent.Context> source,
                                       @Nonnull BlockableEventLoop<?> looper) {
    }

    /**
     * Returns the current server player for the given context. Note that this method returns
     * the sender of the message, and may return null if the connection is interrupted. In
     * this case, the message handling should be ignored.
     *
     * @param source the source of the network event
     * @return the server player, may return null in the future
     * @see #handleServerMessage(int, FriendlyByteBuf, Supplier, BlockableEventLoop)
     */
    @Nullable
    public static ServerPlayer getServerPlayer(@Nonnull Supplier<NetworkEvent.Context> source) {
        return source.get().getNetworkManager().isConnected() ? source.get().getSender() : null;
    }

    /**
     * Allocates a heap/direct buffer to write indexed packet data. The message index is
     * used to identify the type of message, which also affects your network protocol.
     *
     * @param index the message index used on the reception side, ranged from 0 to 65535
     * @return a byte buf to write the packet data (message body)
     */
    @Nonnull
    public PacketBuffer buffer(int index) {
        assert (index >= 0 && index <= 0xFFFF);
        PacketBuffer buffer = new PacketBuffer(mName);
        buffer.writeShort(index);
        return buffer;
    }
}
