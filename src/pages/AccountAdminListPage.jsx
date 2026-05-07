import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import { useAllAccounts } from '../hooks/useAllAccounts';

const ACCOUNT_TYPES = ['ALL', 'CHEQUING', 'SAVINGS', 'TFSA', 'RRSP'];

function StatusBadge({ status }) {
  const className = status === 'ACTIVE' ? 'badge badge-active' : 'badge badge-closed';
  return <span className={className}>{status}</span>;
}

export function AccountAdminListPage() {
  const query = useAllAccounts();
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');

  const accounts = query.data || [];
  const error = query.error ? mapAxiosError(query.error) : null;

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return accounts.filter((a) => {
      const matchesSearch =
        !q ||
        String(a.accountId).toLowerCase().includes(q) ||
        String(a.customerId).toLowerCase().includes(q);
      const matchesType = typeFilter === 'ALL' || a.accountType === typeFilter;
      return matchesSearch && matchesType;
    });
  }, [accounts, search, typeFilter]);

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <h2>All Accounts</h2>
          <p className="muted" style={{ margin: '0.25rem 0 0' }}>Every account across all customers.</p>
        </div>

        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <div className="field" style={{ flex: '1 1 240px', maxWidth: 360 }}>
            <label htmlFor="admin-accounts-search">Search</label>
            <input
              id="admin-accounts-search"
              type="text"
              placeholder="Account ID or Customer ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <div className="field" style={{ flex: '0 0 180px' }}>
            <label htmlFor="admin-accounts-type">Account Type</label>
            <select
              id="admin-accounts-type"
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
            >
              {ACCOUNT_TYPES.map((t) => (
                <option key={t} value={t}>{t === 'ALL' ? 'All Types' : t}</option>
              ))}
            </select>
          </div>
        </div>

        {query.isLoading ? <div className="banner success">Loading accounts…</div> : null}
        {error ? <div className="banner error">{error.message}</div> : null}
      </section>

      <section className="table-shell">
        {error ? (
          <div className="panel">
            <h3>Accounts unavailable</h3>
            <p className="muted">The accounts list cannot be displayed right now.</p>
          </div>
        ) : !query.isLoading && filtered.length === 0 ? (
          <div className="panel">
            <h3>No accounts found</h3>
            <p className="muted">Try adjusting your search or filter.</p>
          </div>
        ) : filtered.length > 0 ? (
          <table>
            <thead>
              <tr>
                <th>Account ID</th>
                <th>Customer ID</th>
                <th>Type</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((account) => (
                <tr key={account.accountId}>
                  <td>
                    <Link
                      to={`/accounts/${account.accountId}`}
                      style={{ fontWeight: 700, color: 'var(--accent)', textDecoration: 'none' }}
                    >
                      #{account.accountId}
                    </Link>
                  </td>
                  <td>
                    <Link
                      to={`/customer/${account.customerId}/accounts`}
                      style={{ fontWeight: 700, color: 'var(--accent)', textDecoration: 'none' }}
                    >
                      {account.customerId}
                    </Link>
                  </td>
                  <td>{account.accountType}</td>
                  <td><StatusBadge status={account.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : null}
      </section>
    </div>
  );
}
