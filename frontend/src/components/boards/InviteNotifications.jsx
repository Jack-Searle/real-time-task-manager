import { Link } from "react-router-dom";

function InviteNotifications({ invites }) {
    if (!invites.length) {
        return null;
    }

    return (
        <div className="invite-notifications">
            {invites.map((invite) => (
                <Link key={invite.id} className="invite-pill" to={`/dashboard/invites/${invite.token}`}>
                    Invite: {invite.boardName}
                </Link>
            ))}
        </div>
    );
}

export default InviteNotifications;
