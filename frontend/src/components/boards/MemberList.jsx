function MemberList({ members, canRemoveMembers = false, onRemoveMember }) {
    const owner = members.find((m) => m.role === "OWNER");
    const otherMembers = members.filter((m) => m.role !== "OWNER");

    const renderMember = (member, removable) => (
        <div className="member-card" key={member.id}>
            <div className="member-card-avatar">
                {(member.firstName?.[0] || member.email?.[0] || "?").toUpperCase()}
            </div>
            <div className="member-card-info">
                <strong>{member.firstName} {member.lastName}</strong>
                <span className="meta">{member.email}</span>
            </div>
            <div className="member-card-right">
                <span className={`member-role-badge ${member.role.toLowerCase()}`}>{member.role}</span>
                {removable && (
                    <button
                        className="icon-button member-remove-btn"
                        type="button"
                        title="Remove member"
                        onClick={() => onRemoveMember(member.id)}
                    >
                        ✕
                    </button>
                )}
            </div>
        </div>
    );

    return (
        <section className="side-panel member-panel">
            <h2>
                Members{" "}
                <span className="member-count">({members.length})</span>
            </h2>
            <div className="member-cards">
                {owner && renderMember(owner, false)}

                {otherMembers.length > 0 && (
                    <div className="member-cards-extra">
                        {otherMembers.map((m) => renderMember(m, canRemoveMembers))}
                    </div>
                )}

                {otherMembers.length > 0 && (
                    <p className="member-expand-hint">
                        +{otherMembers.length} more member{otherMembers.length > 1 ? "s" : ""} — hover to see
                    </p>
                )}
            </div>
        </section>
    );
}

export default MemberList;
