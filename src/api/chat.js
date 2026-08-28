import { accountApiClient } from './axiosClient';

/**
 * Savings Insight Chatbot (BRD 6.1). The backend agent decides for itself which
 * customer data (accounts, spending, savings goals) or knowledge base articles to
 * pull in before answering — this call just sends the raw message and gets back a
 * ready-to-render response, its basis, and whether it's a blocked/limited-data reply.
 */
export async function askSavingsInsight(message) {
  const response = await accountApiClient.post('/api/chat/savings-insights', { message });
  return response.data;
}
