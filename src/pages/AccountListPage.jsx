import { Fragment, useState, useEffect } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { createAccount, getInterestRates } from '../api/accounts';
import { getCustomer, listCustomers } from '../api/customers';
import { mapAxiosError } from '../api/axiosClient';
import { useAuth } from '../auth/AuthContext';
import { useListCustomerAccounts } from '../hooks/useListCustomerAccounts';
import { ACCOUNT_TYPES, emptyCreateAccountForm } from '../types';

// Set to false to test KYC error boundaries
const MOCK_KYC_VERIFIED = true;

export function AccountListPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { customerId } = useParams();
  const { isAdmin, rememberCustomerId } = useAuth();
  const [formState, setFormState] = useState(emptyCreateAccountForm);
  const [error, setError] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);
  // Show registration success message if present, then clear it from history
  useEffect(() => {
    if (location.state?.successMessage) {
      setActionMessage(location.state.successMessage);
      // Remove the successMessage from history so it doesn't persist on refresh or navigation
      navigate(location.pathname, { replace: true });
    }
  }, [location.state, location.pathname, navigate]);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [expandedAccountId, setExpandedAccountId] = useState(null);

  const query = useListCustomerAccounts(customerId);
  const totalBalance = (query.data || []).reduce((sum, acc) => sum + (parseFloat(acc.balance) || 0), 0);
  const customerQuery = useQuery({
    queryKey: ['customer', customerId],
    queryFn: () => getCustomer(customerId),
    enabled: Boolean(customerId)
  });
  const customerListQuery = useQuery({
    queryKey: ['customers'],
    queryFn: listCustomers,
    enabled: isAdmin
  });
  const createAccountMutation = useMutation({ mutationFn: createAccount });
  const interestRatesQuery = useQuery({
    queryKey: ['interest-rates'],
    queryFn: getInterestRates
  });

  const deletedAccountMessage = location.state?.deletedAccountMessage || null;
  const flashMessage = location.state?.flash || null;
  const accountsError = query.error ? mapDeletedCustomerAccountsError(query.error) : null;
  const customerError = customerQuery.error ? mapDeletedCustomerAccountsError(customerQuery.error) : null;

  async function handleCreateAccount(event) {
    event.preventDefault();
    setError(null);
    setActionMessage(null);

    if (existingTypes.has(formState.accountType)) {
      setError({ message: `You already have an active ${formState.accountType} account.` });
      return;
    }

    try {
      const payload = {
        accountType: formState.accountType,
        customerId,
        ...(isAdmin ? { balance: formState.balance, interestRate: formState.interestRate } : {}),
        ...(formState.accountType === 'TFSA' ? { dateOfBirth: customerQuery.data?.dateOfBirth } : {})
      };
      const createdAccount = await createAccountMutation.mutateAsync(payload);
      setActionMessage(`Account ${createdAccount.accountId} created successfully.`);
      setFormState(emptyCreateAccountForm);
      setIsCreateModalOpen(false);
      await Promise.all([query.refetch(), customerQuery.refetch()]);
    } catch (mutationError) {
      const mapped = mapAxiosError(mutationError);
      if (mapped.code === 'TFSA_ALREADY_EXISTS') {
        setError({ ...mapped, message: 'You already have an active TFSA account.' });
      } else {
        setError(mapped);
      }
    }
  }

  function handleCustomerSwitch(event) {
    const nextCustomerId = event.target.value;
    if (!nextCustomerId) {
      return;
    }

    setError(null);
    setActionMessage(null);
    rememberCustomerId(nextCustomerId);
    navigate(`/customer/${nextCustomerId}/accounts`);
  }

  const showInterestRate = isAdmin && (formState.accountType === 'SAVINGS' || formState.accountType === 'TFSA');
  const showBalance = isAdmin && formState.accountType !== 'RRSP';
  const existingTypes = new Set((query.data || []).map((a) => a.accountType));
  const isDuplicateType = existingTypes.has(formState.accountType);
  const isTfsa = formState.accountType === 'TFSA';
  const customerDateOfBirth = customerQuery.data?.dateOfBirth || null;
  const customerKycVerified = MOCK_KYC_VERIFIED;
  const age = calculateAge(customerDateOfBirth);
  const isTooYoung = isTfsa && age !== null && age < 18;
  const isKycBlocked = isTfsa && !customerKycVerified;
  const tfsaRate = interestRatesQuery.data?.TFSA ?? interestRatesQuery.data?.tfsa ?? null;
  const rrspRate = interestRatesQuery.data?.RRSP ?? interestRatesQuery.data?.rrsp ?? null;

  function openCreateModal() {
    setError(null);
    setActionMessage(null);
    setIsCreateModalOpen(true);
  }

  function closeCreateModal() {
    if (createAccountMutation.isPending) {
      return;
    }

    setIsCreateModalOpen(false);
    setError(null);
  }

  return (
    <>
      {/* Banner messages at the very top, outside main content */}
      <div className="banner-stack" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
        {deletedAccountMessage ? <div className="banner success">{deletedAccountMessage}</div> : null}
        {flashMessage ? <div className="banner info">{flashMessage}</div> : null}
        {actionMessage ? <div className="banner success">{actionMessage}</div> : null}
        {error && !isCreateModalOpen ? <div className="banner error">{error.message}</div> : null}
        {query.isLoading ? <div className="banner success">Loading accounts...</div> : null}
        {accountsError ? <div className="banner error">{accountsError.message}</div> : null}
        {customerError ? <div className="banner error">{customerError.message}</div> : null}
      </div>
      <div className="stack">
        <section className="panel stack">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <h2>My Accounts</h2>
              <p className="muted" style={{ margin: '0.25rem 0 0' }}>View and manage your accounts.</p>
            </div>
            {!accountsError && !customerError ? (
              <button type="button" onClick={openCreateModal}>Create Account</button>
            ) : null}
          </div>
          {isAdmin ? (
            <div className="field">
              <label htmlFor="accounts-customer-switcher">Admin Customer Switcher</label>
              <select id="accounts-customer-switcher" value={customerId || ''} onChange={handleCustomerSwitch}>
                <option value="">Select customer</option>
                {(customerListQuery.data || []).map((customerOption) => (
                  <option key={customerOption.customerId} value={customerOption.customerId}>
                    {customerOption.customerId} - {customerOption.name}
                  </option>
                ))}
              </select>
              {customerListQuery.error ? <p className="field-hint">{mapAxiosError(customerListQuery.error).message}</p> : null}
            </div>
          ) : null}
          <section className="accounts-table-shell">
            {accountsError || customerError ? (
              <div className="panel">
                <h3>Accounts unavailable</h3>
                <p className="muted">The selected customer's accounts cannot be displayed right now.</p>
              </div>
            ) : query.data && query.data.length > 0 ? (
              <div className="account-card-list">
                {query.data.map((account) => (
                  <Link key={account.accountId} className="account-card" to={`/accounts/${account.accountId}`}>
                    <div className="account-card-header">
                      <span className="account-card-type">
                        {account.accountType}
                        {account.accountType === 'TFSA' ? <span className="badge" style={{ marginLeft: '0.4rem' }}>Tax-Free</span> : null}
                      </span>
                      <span className="account-card-id">{account.accountId}</span>
                    </div>
                    <span className="account-card-balance">${account.balance}</span>
                    {account.accountType === 'TFSA' && tfsaRate !== null ? (
                      <span className="field-hint">{tfsaRate}% APY</span>
                    ) : null}
                  </Link>
                ))}
                <div className="account-card account-card-total">
                  <div className="account-card-header">
                    <span className="account-card-type">Total Account Balance</span>
                  </div>
                  <span className="account-card-balance">${totalBalance.toFixed(2)}</span>
                </div>
              </div>
            ) : (
              <div className="panel">
                <h3>No active accounts</h3>
              </div>
            )}
          </section>
        </section>

      {isCreateModalOpen ? (
        <div className="modal-backdrop" onClick={closeCreateModal}>
          <div className="modal-panel stack" role="dialog" aria-modal="true" aria-labelledby="create-account-modal-title" onClick={(event) => event.stopPropagation()}>
            <div className="section-header">
              <div>
                <h3 id="create-account-modal-title">Create Account</h3>
              </div>
              <button type="button" className="secondary" onClick={closeCreateModal} disabled={createAccountMutation.isPending}>Close</button>
            </div>
            <form className="stack" onSubmit={handleCreateAccount}>
              {error ? <div className="banner error">{error.message}</div> : null}
              {isKycBlocked ? (
                <div className="banner error">Identity verification required for this account type.</div>
              ) : null}
              {isTooYoung ? (
                <div className="banner error">TFSA requires a minimum age of 18.</div>
              ) : null}
              <div className="stack">
                <div className="field">
                  <label htmlFor="accountType">Account Type</label>
                  <select
                    id="accountType"
                    value={formState.accountType}
                    onChange={(event) => setFormState((current) => ({ ...current, accountType: event.target.value }))}
                  >
                    {ACCOUNT_TYPES.map((type) => (
                      <option key={type} value={type} disabled={type === 'TFSA' && !customerKycVerified}>{type}</option>
                    ))}
                  </select>
                  {isTfsa && tfsaRate !== null ? (
                    <p className="field-hint">Current TFSA rate: {tfsaRate}% APY</p>
                  ) : null}
                  {formState.accountType === 'RRSP' && rrspRate !== null ? (
                    <p className="field-hint">Current RRSP rate: {rrspRate}% APY</p>
                  ) : null}
                </div>
                {showBalance ? (
                  <div className="field">
                    <label htmlFor="balance">{isTfsa ? 'Initial Contribution' : 'Opening Balance'}</label>
                    <input
                      id="balance"
                      value={formState.balance}
                      onChange={(event) => setFormState((current) => ({ ...current, balance: event.target.value }))}
                    />
                  </div>
                ) : null}
                {showInterestRate ? (
                  <div className="field">
                    <label htmlFor="interestRate">Interest Rate</label>
                    <input
                      id="interestRate"
                      value={formState.interestRate}
                      onChange={(event) => setFormState((current) => ({ ...current, interestRate: event.target.value }))}
                    />
                  </div>
                ) : null}
              </div>
              <div className="actions centered-actions">
                <button type="submit" disabled={createAccountMutation.isPending || isTooYoung || isKycBlocked}>Create Account</button>
                <button
                  type="button"
                  className="secondary"
                  onClick={() => {
                    setFormState(emptyCreateAccountForm);
                    setError(null);
                  }}
                  disabled={createAccountMutation.isPending}
                >
                  Reset
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      
      </div>
    </>
  );
}

function calculateAge(dateOfBirth) {
  if (!dateOfBirth) return null;
  const dob = new Date(dateOfBirth);
  if (isNaN(dob.getTime())) return null;
  const today = new Date();
  let age = today.getFullYear() - dob.getFullYear();
  const monthDiff = today.getMonth() - dob.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
    age--;
  }
  return age;
}

function mapDeletedCustomerAccountsError(error) {
  const mapped = mapAxiosError(error);

  if (mapped.code === 'CUSTOMER_NOT_FOUND' || mapped.message === 'Customer not found') {
    return {
      ...mapped,
      message: 'This customer may have been deleted or is no longer accessible, so their accounts cannot be shown.'
    };
  }

  return mapped;
}
